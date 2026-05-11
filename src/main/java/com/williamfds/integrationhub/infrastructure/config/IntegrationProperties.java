package com.williamfds.integrationhub.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("integration")
public record IntegrationProperties(
        Idempotency idempotency,
        Retry retry,
        RateLimit rateLimit
) {
    public record Idempotency(Duration ttl) {}

    public record Retry(int maxAttempts, Duration initialInterval, double multiplier, Duration maxInterval) {}

    public record RateLimit(Bucket shopifyWebhook) {
        public record Bucket(int limit, Duration window) {}
    }
}
