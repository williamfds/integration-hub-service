package com.williamfds.integrationhub.infrastructure.idempotency;

import com.williamfds.integrationhub.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RedisIdempotencyStoreIT extends AbstractIntegrationIT {

    @Autowired
    IdempotencyStore store;

    @Test
    void first_register_returns_true_second_returns_false() {
        var record = new IdempotencyRecord("wh-1", "shopify", Instant.now(), IdempotencyStatus.RECEIVED);

        boolean first = store.tryRegister("webhook:shopify:order:wh-1", record, Duration.ofMinutes(1));
        boolean second = store.tryRegister("webhook:shopify:order:wh-1", record, Duration.ofMinutes(1));

        assertThat(first).isTrue();
        assertThat(second).isFalse();
    }

    @Test
    void update_status_replaces_value_preserving_metadata() {
        var record = new IdempotencyRecord("wh-2", "shopify", Instant.now(), IdempotencyStatus.RECEIVED);
        String key = "webhook:shopify:order:wh-2";

        store.tryRegister(key, record, Duration.ofMinutes(1));
        store.updateStatus(key, IdempotencyStatus.PROCESSED, Duration.ofMinutes(1));

        var stored = store.find(key).orElseThrow();
        assertThat(stored.status()).isEqualTo(IdempotencyStatus.PROCESSED);
        assertThat(stored.webhookId()).isEqualTo("wh-2");
        assertThat(stored.platform()).isEqualTo("shopify");
    }
}
