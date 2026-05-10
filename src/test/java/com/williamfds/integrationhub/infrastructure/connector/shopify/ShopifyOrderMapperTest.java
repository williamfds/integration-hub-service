package com.williamfds.integrationhub.infrastructure.connector.shopify;

import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopifyOrderMapperTest {

    @Test
    void maps_paid_order_to_canonical() {
        var payload = new ShopifyOrderPayload(
                450789469L,
                "199.65",
                "USD",
                "paid",
                null,
                new ShopifyOrderPayload.Customer("Bob", "Norman", "bob@example.com")
        );

        var order = ShopifyOrderMapper.toCanonical(payload);

        assertThat(order.platform()).isEqualTo(Platform.SHOPIFY);
        assertThat(order.externalId()).isEqualTo("450789469");
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("199.65"));
        assertThat(order.currency()).isEqualTo("USD");
        assertThat(order.customerName()).isEqualTo("Bob Norman");
        assertThat(order.customerEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void fulfilled_overrides_paid_when_present() {
        var payload = new ShopifyOrderPayload(1L, "10.00", "BRL", "paid", "fulfilled", null);

        var order = ShopifyOrderMapper.toCanonical(payload);

        assertThat(order.status()).isEqualTo(OrderStatus.FULFILLED);
        assertThat(order.customerName()).isNull();
    }

    @Test
    void refunded_takes_precedence() {
        var payload = new ShopifyOrderPayload(1L, "10.00", "BRL", "refunded", "fulfilled", null);

        assertThat(ShopifyOrderMapper.toCanonical(payload).status()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    void rejects_payload_missing_id() {
        var payload = new ShopifyOrderPayload(null, "10.00", "BRL", "paid", null, null);

        assertThatThrownBy(() -> ShopifyOrderMapper.toCanonical(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }

    @Test
    void rejects_non_numeric_total_price() {
        var payload = new ShopifyOrderPayload(1L, "not-a-number", "BRL", "paid", null, null);

        assertThatThrownBy(() -> ShopifyOrderMapper.toCanonical(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("total_price");
    }
}
