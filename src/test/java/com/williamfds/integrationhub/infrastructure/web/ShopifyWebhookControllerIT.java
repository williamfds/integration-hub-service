package com.williamfds.integrationhub.infrastructure.web;

import com.williamfds.integrationhub.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ShopifyWebhookControllerIT extends AbstractPostgresIT {

    @Autowired
    MockMvc mvc;

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
    void accepts_payload_and_persists_canonical_order() throws Exception {
        mvc.perform(post("/webhooks/shopify/orders")
                        .header("X-Webhook-Id", "test-webhook-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.platform").value("SHOPIFY"))
                .andExpect(jsonPath("$.externalId").value("450789469"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.currency").value("USD"));

        mvc.perform(get("/orders").param("platform", "SHOPIFY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].externalId").value("450789469"));
    }

    @Test
    void rejects_invalid_payload_with_400() throws Exception {
        String bad = """
                { "id": 1, "total_price": "abc", "currency": "USD" }
                """;

        mvc.perform(post("/webhooks/shopify/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bad))
                .andExpect(status().isBadRequest());
    }
}
