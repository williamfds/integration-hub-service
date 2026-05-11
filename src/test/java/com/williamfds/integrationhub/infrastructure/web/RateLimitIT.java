package com.williamfds.integrationhub.infrastructure.web;

import com.williamfds.integrationhub.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "integration.rate-limit.shopify-webhook.limit=5",
        "integration.rate-limit.shopify-webhook.window=PT10S"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RateLimitIT extends AbstractIntegrationIT {

    @Autowired
    MockMvc mvc;

    private static final String PAYLOAD = """
            {
              "id": 1,
              "total_price": "10.00",
              "currency": "USD",
              "financial_status": "paid",
              "customer": null
            }
            """;

    @Test
    void blocks_request_after_limit_is_reached() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mvc.perform(post("/webhooks/shopify/orders")
                            .header("X-Webhook-Id", "wh-rl-" + i)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(PAYLOAD))
                    .andExpect(status().isAccepted());
        }

        mvc.perform(post("/webhooks/shopify/orders")
                        .header("X-Webhook-Id", "wh-rl-6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
