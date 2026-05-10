package com.williamfds.integrationhub.infrastructure.persistence;

import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
class OrderEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Platform platform;

    @Column(name = "external_id", nullable = false, length = 128)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderEntity() {}

    OrderEntity(UUID id, Platform platform, String externalId, OrderStatus status,
                BigDecimal totalAmount, String currency, String customerName,
                String customerEmail, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.platform = platform;
        this.externalId = externalId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId()                 { return id; }
    Platform getPlatform()       { return platform; }
    String getExternalId()       { return externalId; }
    OrderStatus getStatus()      { return status; }
    BigDecimal getTotalAmount()  { return totalAmount; }
    String getCurrency()         { return currency; }
    String getCustomerName()     { return customerName; }
    String getCustomerEmail()    { return customerEmail; }
    Instant getCreatedAt()       { return createdAt; }
    Instant getUpdatedAt()       { return updatedAt; }

    void setStatus(OrderStatus status)       { this.status = status; }
    void setUpdatedAt(Instant updatedAt)     { this.updatedAt = updatedAt; }
}
