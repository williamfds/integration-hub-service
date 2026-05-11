package com.williamfds.integrationhub.infrastructure.messaging;

public record ShopifyWebhookMessage(String webhookId, String payload) {}
