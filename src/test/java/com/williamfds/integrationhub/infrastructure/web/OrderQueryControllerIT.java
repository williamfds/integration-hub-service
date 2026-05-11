package com.williamfds.integrationhub.infrastructure.web;

import com.williamfds.integrationhub.AbstractIntegrationIT;
import com.williamfds.integrationhub.domain.model.Order;
import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;
import com.williamfds.integrationhub.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class OrderQueryControllerIT extends AbstractIntegrationIT {

    @Autowired MockMvc mvc;
    @Autowired OrderRepository orders;

    @Test
    void returns_order_by_id() throws Exception {
        var saved = orders.save(Order.newCanonical(
                Platform.SHOPIFY, "ext-1", OrderStatus.PAID,
                new BigDecimal("42.00"), "BRL", "Alice", "alice@example.com"));

        mvc.perform(get("/orders/" + saved.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.id().toString()))
                .andExpect(jsonPath("$.externalId").value("ext-1"));
    }

    @Test
    void returns_404_for_missing_order() throws Exception {
        mvc.perform(get("/orders/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
