package com.williamfds.integrationhub.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamfds.integrationhub.application.usecase.RegisterIncomingOrder;
import com.williamfds.integrationhub.domain.exception.NonRecoverableException;
import com.williamfds.integrationhub.infrastructure.config.IntegrationProperties;
import com.williamfds.integrationhub.infrastructure.connector.shopify.ShopifyOrderMapper;
import com.williamfds.integrationhub.infrastructure.connector.shopify.ShopifyOrderPayload;
import com.williamfds.integrationhub.infrastructure.idempotency.IdempotencyStatus;
import com.williamfds.integrationhub.infrastructure.idempotency.IdempotencyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ShopifyOrderWebhookListener {

    private static final Logger log = LoggerFactory.getLogger(ShopifyOrderWebhookListener.class);

    private final ObjectMapper mapper;
    private final RegisterIncomingOrder registerIncomingOrder;
    private final IdempotencyStore idempotencyStore;
    private final IntegrationProperties props;

    public ShopifyOrderWebhookListener(
            ObjectMapper mapper,
            RegisterIncomingOrder registerIncomingOrder,
            IdempotencyStore idempotencyStore,
            IntegrationProperties props
    ) {
        this.mapper = mapper;
        this.registerIncomingOrder = registerIncomingOrder;
        this.idempotencyStore = idempotencyStore;
        this.props = props;
    }

    @RabbitListener(queues = RabbitTopology.SHOPIFY_WEBHOOK_QUEUE)
    public void onMessage(ShopifyWebhookMessage message) {
        MDC.put("webhookId", message.webhookId());
        MDC.put("platform", "shopify");
        try {
            log.info("Consuming shopify webhook");
            try {
                var payload = parsePayload(message);
                var canonical = mapToCanonical(payload);
                registerIncomingOrder.handle(canonical);
            } catch (NonRecoverableException e) {
                log.error("Routing non-recoverable webhook to DLQ: {}", e.getMessage());
                throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
            }

            String key = idempotencyKey(message.webhookId());
            idempotencyStore.updateStatus(key, IdempotencyStatus.PROCESSED, props.idempotency().ttl());
            log.info("Shopify webhook processed");
        } finally {
            MDC.clear();
        }
    }

    private ShopifyOrderPayload parsePayload(ShopifyWebhookMessage message) {
        try {
            return mapper.readValue(message.payload(), ShopifyOrderPayload.class);
        } catch (JsonProcessingException e) {
            throw new NonRecoverableException("Malformed Shopify payload", e);
        }
    }

    private static com.williamfds.integrationhub.domain.model.Order mapToCanonical(ShopifyOrderPayload payload) {
        try {
            return ShopifyOrderMapper.toCanonical(payload);
        } catch (IllegalArgumentException e) {
            throw new NonRecoverableException("Invalid Shopify payload", e);
        }
    }

    private static String idempotencyKey(String webhookId) {
        return "webhook:shopify:order:" + webhookId;
    }
}
