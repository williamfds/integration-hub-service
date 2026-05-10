package com.williamfds.integrationhub.application.usecase;

import com.williamfds.integrationhub.domain.model.Order;
import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;
import com.williamfds.integrationhub.domain.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ListOrders {

    private final OrderRepository orders;

    public ListOrders(OrderRepository orders) {
        this.orders = orders;
    }

    public OrderRepository.Page<Order> handle(Optional<Platform> platform,
                                              Optional<OrderStatus> status,
                                              int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, 100);
        return orders.search(platform, status, safePage, safeSize);
    }
}
