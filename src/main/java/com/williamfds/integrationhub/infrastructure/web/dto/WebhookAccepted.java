package com.williamfds.integrationhub.infrastructure.web.dto;

public record WebhookAccepted(String webhookId, String status, String detail) {

    public static WebhookAccepted received(String webhookId) {
        return new WebhookAccepted(webhookId, "received", null);
    }

    public static WebhookAccepted duplicate(String webhookId) {
        return new WebhookAccepted(webhookId, "duplicate", "webhook already received");
    }
}
