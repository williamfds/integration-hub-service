package com.williamfds.integrationhub.application.usecase;

import com.williamfds.integrationhub.domain.exception.OrderNotFoundException;
import com.williamfds.integrationhub.domain.model.Order;
import com.williamfds.integrationhub.domain.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FindOrder {

    private final OrderRepository orders;

    public FindOrder(OrderRepository orders) {
        this.orders = orders;
    }

    public Order byId(UUID id) {
        return orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
