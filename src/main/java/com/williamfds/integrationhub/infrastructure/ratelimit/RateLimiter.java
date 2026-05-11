package com.williamfds.integrationhub.infrastructure.ratelimit;

import java.time.Duration;

public interface RateLimiter {

    Decision tryAcquire(String bucket);

    record Decision(boolean allowed, Duration retryAfter) {
        public static Decision allow() {
            return new Decision(true, Duration.ZERO);
        }

        public static Decision deny(Duration retryAfter) {
            return new Decision(false, retryAfter);
        }
    }
}
