package com.williamfds.integrationhub.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopology {

    public static final String EVENTS_EXCHANGE = "integration.events";
    public static final String DLX_EXCHANGE = "integration.events.dlx";
    public static final String SHOPIFY_WEBHOOK_QUEUE = "shopify.order.webhook.received";
    public static final String SHOPIFY_WEBHOOK_DLQ = "shopify.order.webhook.received.dlq";
    public static final String SHOPIFY_WEBHOOK_ROUTING_KEY = "shopify.order.received";

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    FanoutExchange deadLetterExchange() {
        return new FanoutExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    Queue shopifyWebhookReceivedQueue() {
        return QueueBuilder.durable(SHOPIFY_WEBHOOK_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    Queue shopifyWebhookReceivedDlq() {
        return QueueBuilder.durable(SHOPIFY_WEBHOOK_DLQ).build();
    }

    @Bean
    Binding shopifyWebhookBinding(Queue shopifyWebhookReceivedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(shopifyWebhookReceivedQueue)
                .to(eventsExchange)
                .with(SHOPIFY_WEBHOOK_ROUTING_KEY);
    }

    @Bean
    Binding shopifyWebhookDlqBinding(Queue shopifyWebhookReceivedDlq, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(shopifyWebhookReceivedDlq).to(deadLetterExchange);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
