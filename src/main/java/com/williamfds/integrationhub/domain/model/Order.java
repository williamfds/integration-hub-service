package com.williamfds.integrationhub.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Order(
        UUID id,
        Platform platform,
        String externalId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        String customerName,
        String customerEmail,
        Instant createdAt,
        Instant updatedAt
) {
    public Order {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(totalAmount, "totalAmount");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");

        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("externalId must not be blank");
        }
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException("currency must be a 3-letter ISO 4217 code");
        }
        if (totalAmount.signum() < 0) {
            throw new IllegalArgumentException("totalAmount must be non-negative");
        }
    }

    public static Order newCanonical(
            Platform platform,
            String externalId,
            OrderStatus status,
            BigDecimal totalAmount,
            String currency,
            String customerName,
            String customerEmail
    ) {
        Instant now = Instant.now();
        return new Order(UUID.randomUUID(), platform, externalId, status,
                totalAmount, currency, customerName, customerEmail, now, now);
    }

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, platform, externalId, newStatus, totalAmount, currency,
                customerName, customerEmail, createdAt, Instant.now());
    }
}
