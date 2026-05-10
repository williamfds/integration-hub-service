package com.williamfds.integrationhub.infrastructure.connector.shopify;

import com.williamfds.integrationhub.domain.model.Order;
import com.williamfds.integrationhub.domain.model.OrderStatus;
import com.williamfds.integrationhub.domain.model.Platform;

import java.math.BigDecimal;

public final class ShopifyOrderMapper {

    private ShopifyOrderMapper() {}

    public static Order toCanonical(ShopifyOrderPayload payload) {
        if (payload == null || payload.id() == null) {
            throw new IllegalArgumentException("Shopify payload missing id");
        }
        if (payload.totalPrice() == null || payload.totalPrice().isBlank()) {
            throw new IllegalArgumentException("Shopify payload missing total_price");
        }
        if (payload.currency() == null) {
            throw new IllegalArgumentException("Shopify payload missing currency");
        }

        BigDecimal total;
        try {
            total = new BigDecimal(payload.totalPrice());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid total_price: " + payload.totalPrice(), e);
        }

        var status = OrderStatus.fromShopify(payload.financialStatus(), payload.fulfillmentStatus());
        var customer = payload.customer();
        String name = customer == null ? null : joinName(customer.firstName(), customer.lastName());
        String email = customer == null ? null : customer.email();

        return Order.newCanonical(
                Platform.SHOPIFY,
                String.valueOf(payload.id()),
                status,
                total,
                payload.currency().toUpperCase(),
                name,
                email
        );
    }

    private static String joinName(String first, String last) {
        String f = first == null ? "" : first.trim();
        String l = last  == null ? "" : last.trim();
        String joined = (f + " " + l).trim();
        return joined.isEmpty() ? null : joined;
    }
}
