package com.williamfds.integrationhub.infrastructure.web;

import com.williamfds.integrationhub.AbstractIntegrationIT;
import com.williamfds.integrationhub.domain.model.Platform;
import com.williamfds.integrationhub.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ShopifyWebhookControllerIT extends AbstractIntegrationIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    OrderRepository orders;

    private static final String PAYLOAD = """
            {
              "id": 450789469,
              "total_price": "199.65",
              "currency": "USD",
              "financial_status": "paid",
              "fulfillment_status": null,
              "customer": { "first_name": "Bob", "last_name": "Norman", "email": "bob@example.com" }
            }
            """;

    @Test
    void accepts_payload_and_consumer_persists_canonical_order() throws Exception {
        mvc.perform(post("/webhooks/shopify/orders")
                        .header("X-Webhook-Id", "webhook-async-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.webhookId").value("webhook-async-1"))
                .andExpect(jsonPath("$.status").value("received"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(orders.findByPlatformAndExternalId(Platform.SHOPIFY, "450789469"))
                        .as("consumer should persist canonical order")
                        .isPresent());
    }

    @Test
    void duplicate_webhook_returns_duplicate_and_persists_once() throws Exception {
        String headerId = "webhook-async-dup";

        mvc.perform(post("/webhooks/shopify/orders")
                        .header("X-Webhook-Id", headerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("received"));

        mvc.perform(post("/webhooks/shopify/orders")
                        .header("X-Webhook-Id", headerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("duplicate"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(orders.findByPlatformAndExternalId(Platform.SHOPIFY, "450789469"))
                        .isPresent());
    }

    @Test
    void rejects_missing_webhook_id_header_with_400() throws Exception {
        mvc.perform(post("/webhooks/shopify/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isBadRequest());
    }
}
