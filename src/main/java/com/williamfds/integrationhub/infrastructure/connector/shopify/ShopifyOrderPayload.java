package com.williamfds.integrationhub.infrastructure.connector.shopify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopifyOrderPayload(
        Long id,
        @JsonProperty("total_price")      String totalPrice,
        String                            currency,
        @JsonProperty("financial_status") String financialStatus,
        @JsonProperty("fulfillment_status") String fulfillmentStatus,
        Customer customer
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Customer(
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name")  String lastName,
            String                      email
    ) {}
}
