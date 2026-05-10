package com.williamfds.integrationhub.domain.model;

public enum OrderStatus {
    PENDING,
    PAID,
    FULFILLED,
    CANCELLED,
    REFUNDED;

    public static OrderStatus fromShopify(String financialStatus, String fulfillmentStatus) {
        if ("refunded".equalsIgnoreCase(financialStatus)) return REFUNDED;
        if ("voided".equalsIgnoreCase(financialStatus))   return CANCELLED;
        if ("fulfilled".equalsIgnoreCase(fulfillmentStatus)) return FULFILLED;
        if ("paid".equalsIgnoreCase(financialStatus))     return PAID;
        return PENDING;
    }
}
