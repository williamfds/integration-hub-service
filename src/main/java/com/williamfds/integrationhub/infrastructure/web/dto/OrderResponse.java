package com.williamfds.integrationhub.infrastructure.web.dto;

import com.williamfds.integrationhub.domain.model.Order;
import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
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
    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.id(), o.platform(), o.externalId(), o.status(),
                o.totalAmount(), o.currency(), o.customerName(), o.customerEmail(),
                o.createdAt(), o.updatedAt()
        );
    }
}
