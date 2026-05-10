package com.williamfds.integrationhub.infrastructure.persistence;

import com.williamfds.integrationhub.domain.model.Order;
import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;
import com.williamfds.integrationhub.domain.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpa;

    OrderRepositoryAdapter(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        OrderEntity existing = jpa.findById(order.id()).orElse(null);
        if (existing == null) {
            return toDomain(jpa.save(toEntity(order)));
        }
        existing.setStatus(order.status());
        existing.setUpdatedAt(order.updatedAt());
        return toDomain(jpa.save(existing));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpa.findById(id).map(OrderRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Order> findByPlatformAndExternalId(Platform platform, String externalId) {
        return jpa.findByPlatformAndExternalId(platform, externalId).map(OrderRepositoryAdapter::toDomain);
    }

    @Override
    public Page<Order> search(Optional<Platform> platform, Optional<OrderStatus> status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = jpa.search(platform.orElse(null), status.orElse(null), pageable);
        List<Order> items = result.getContent().stream().map(OrderRepositoryAdapter::toDomain).toList();
        return new Page<>(items, result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private static OrderEntity toEntity(Order o) {
        return new OrderEntity(
                o.id(), o.platform(), o.externalId(), o.status(),
                o.totalAmount(), o.currency(), o.customerName(), o.customerEmail(),
                o.createdAt(), o.updatedAt()
        );
    }

    private static Order toDomain(OrderEntity e) {
        return new Order(
                e.getId(), e.getPlatform(), e.getExternalId(), e.getStatus(),
                e.getTotalAmount(), e.getCurrency(), e.getCustomerName(), e.getCustomerEmail(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
