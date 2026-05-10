package com.williamfds.integrationhub.domain.repository;

import com.williamfds.integrationhub.domain.model.Order;
import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findByPlatformAndExternalId(Platform platform, String externalId);

    Page<Order> search(Optional<Platform> platform, Optional<OrderStatus> status, int page, int size);

    record Page<T>(List<T> items, int page, int size, long totalElements) {}
}
