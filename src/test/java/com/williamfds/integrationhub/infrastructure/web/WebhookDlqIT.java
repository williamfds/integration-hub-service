package com.williamfds.integrationhub.infrastructure.web;

import com.williamfds.integrationhub.AbstractIntegrationIT;
import com.williamfds.integrationhub.infrastructure.messaging.RabbitTopology;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WebhookDlqIT extends AbstractIntegrationIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void malformed_payload_lands_in_dlq() throws Exception {
        rabbitTemplate.setReceiveTimeout(500);

        mvc.perform(post("/webhooks/shopify/orders")
                        .header("X-Webhook-Id", "wh-dlq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"id\": 1, \"total_price\": \"abc\", \"currency\": \"USD\" }"))
                .andExpect(status().isAccepted());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Message dlq = rabbitTemplate.receive(RabbitTopology.SHOPIFY_WEBHOOK_DLQ);
            assertThat(dlq).as("message should land in DLQ").isNotNull();
        });
    }
}
