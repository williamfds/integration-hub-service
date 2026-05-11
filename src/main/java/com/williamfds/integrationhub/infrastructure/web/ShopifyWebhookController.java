package com.williamfds.integrationhub.infrastructure.web;

import com.williamfds.integrationhub.infrastructure.config.IntegrationProperties;
import com.williamfds.integrationhub.infrastructure.idempotency.IdempotencyRecord;
import com.williamfds.integrationhub.infrastructure.idempotency.IdempotencyStatus;
import com.williamfds.integrationhub.infrastructure.idempotency.IdempotencyStore;
import com.williamfds.integrationhub.infrastructure.messaging.ShopifyWebhookMessage;
import com.williamfds.integrationhub.infrastructure.messaging.ShopifyWebhookPublisher;
import com.williamfds.integrationhub.infrastructure.ratelimit.RateLimiter;
import com.williamfds.integrationhub.infrastructure.web.dto.WebhookAccepted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/webhooks/shopify")
public class ShopifyWebhookController {

    private static final Logger log = LoggerFactory.getLogger(ShopifyWebhookController.class);
    private static final String PLATFORM = "shopify";
    private static final String RATE_LIMIT_BUCKET = "shopify-webhook";

    private final RateLimiter rateLimiter;
    private final IdempotencyStore idempotencyStore;
    private final ShopifyWebhookPublisher publisher;
    private final IntegrationProperties props;

    public ShopifyWebhookController(
            RateLimiter rateLimiter,
            IdempotencyStore idempotencyStore,
            ShopifyWebhookPublisher publisher,
            IntegrationProperties props
    ) {
        this.rateLimiter = rateLimiter;
        this.idempotencyStore = idempotencyStore;
        this.publisher = publisher;
        this.props = props;
    }

    @PostMapping("/orders")
    public ResponseEntity<WebhookAccepted> receiveOrder(
            @RequestHeader("X-Webhook-Id") String webhookId,
            // TODO (Semana 3): validar header X-Shopify-Hmac-Sha256 contra o payload bruto.
            @RequestBody String rawPayload
    ) {
        MDC.put("webhookId", webhookId);
        MDC.put("platform", PLATFORM);
        try {
            var rateDecision = rateLimiter.tryAcquire(RATE_LIMIT_BUCKET);
            if (!rateDecision.allowed()) {
                log.warn("Rate limit exceeded");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", String.valueOf(rateDecision.retryAfter().toSeconds()))
                        .build();
            }

            String key = idempotencyKey(webhookId);
            var record = new IdempotencyRecord(webhookId, PLATFORM, Instant.now(), IdempotencyStatus.RECEIVED);
            boolean claimed = idempotencyStore.tryRegister(key, record, props.idempotency().ttl());
            if (!claimed) {
                log.info("Duplicate shopify webhook ignored");
                return ResponseEntity.accepted().body(WebhookAccepted.duplicate(webhookId));
            }

            publisher.publish(new ShopifyWebhookMessage(webhookId, rawPayload));
            idempotencyStore.updateStatus(key, IdempotencyStatus.PUBLISHED, props.idempotency().ttl());
            log.info("Shopify webhook published");

            return ResponseEntity.accepted().body(WebhookAccepted.received(webhookId));
        } finally {
            MDC.clear();
        }
    }

    private static String idempotencyKey(String webhookId) {
        return "webhook:shopify:order:" + webhookId;
    }
}
