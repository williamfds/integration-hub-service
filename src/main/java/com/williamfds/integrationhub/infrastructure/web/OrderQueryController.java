package com.williamfds.integrationhub.infrastructure.web;

import com.williamfds.integrationhub.application.usecase.FindOrder;
import com.williamfds.integrationhub.application.usecase.ListOrders;
import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;
import com.williamfds.integrationhub.infrastructure.web.dto.OrderResponse;
import com.williamfds.integrationhub.infrastructure.web.dto.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderQueryController {

    private final FindOrder findOrder;
    private final ListOrders listOrders;

    public OrderQueryController(FindOrder findOrder, ListOrders listOrders) {
        this.findOrder = findOrder;
        this.listOrders = listOrders;
    }

    @GetMapping("/{id}")
    public OrderResponse byId(@PathVariable UUID id) {
        return OrderResponse.from(findOrder.byId(id));
    }

    @GetMapping
    public PageResponse<OrderResponse> list(
            @RequestParam Optional<Platform> platform,
            @RequestParam Optional<OrderStatus> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = listOrders.handle(platform, status, page, size);
        return PageResponse.of(result, OrderResponse::from);
    }
}
