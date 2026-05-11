package com.williamfds.integrationhub.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShopifyWebhookPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ShopifyWebhookPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(ShopifyWebhookMessage message) {
        // TODO (Semana 3): outbox pattern para at-least-once entre Redis e Rabbit.
        rabbitTemplate.convertAndSend(
                RabbitTopology.EVENTS_EXCHANGE,
                RabbitTopology.SHOPIFY_WEBHOOK_ROUTING_KEY,
                message);
    }
}
