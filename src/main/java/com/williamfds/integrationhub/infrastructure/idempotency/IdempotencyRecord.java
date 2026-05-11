package com.williamfds.integrationhub.infrastructure.idempotency;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record IdempotencyRecord(
        String webhookId,
        String platform,
        Instant receivedAt,
        IdempotencyStatus status
) {
    @JsonCreator
    public IdempotencyRecord(
            @JsonProperty("webhookId") String webhookId,
            @JsonProperty("platform") String platform,
            @JsonProperty("receivedAt") Instant receivedAt,
            @JsonProperty("status") IdempotencyStatus status
    ) {
        this.webhookId = webhookId;
        this.platform = platform;
        this.receivedAt = receivedAt;
        this.status = status;
    }

    public IdempotencyRecord withStatus(IdempotencyStatus newStatus) {
        return new IdempotencyRecord(webhookId, platform, receivedAt, newStatus);
    }
}
