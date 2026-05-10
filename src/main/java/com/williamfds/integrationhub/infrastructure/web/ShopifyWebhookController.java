package com.williamfds.integrationhub.infrastructure.web;

import com.williamfds.integrationhub.application.usecase.RegisterIncomingOrder;
import com.williamfds.integrationhub.infrastructure.connector.shopify.ShopifyOrderMapper;
import com.williamfds.integrationhub.infrastructure.connector.shopify.ShopifyOrderPayload;
import com.williamfds.integrationhub.infrastructure.web.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/shopify")
public class ShopifyWebhookController {

    private static final Logger log = LoggerFactory.getLogger(ShopifyWebhookController.class);

    private final RegisterIncomingOrder registerIncomingOrder;

    public ShopifyWebhookController(RegisterIncomingOrder registerIncomingOrder) {
        this.registerIncomingOrder = registerIncomingOrder;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> receiveOrder(
            @RequestHeader(value = "X-Webhook-Id", required = false) String webhookId,
            // TODO (Semana 2): validar header X-Shopify-Hmac-Sha256 contra o payload bruto.
            @RequestBody ShopifyOrderPayload payload
    ) {
        log.info("Shopify webhook received: webhookId={}, externalId={}", webhookId, payload.id());

        var canonical = ShopifyOrderMapper.toCanonical(payload);
        var saved = registerIncomingOrder.handle(canonical);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(OrderResponse.from(saved));
    }
}
