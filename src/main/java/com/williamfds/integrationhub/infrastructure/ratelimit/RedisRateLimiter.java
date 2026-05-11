package com.williamfds.integrationhub.infrastructure.ratelimit;

import com.williamfds.integrationhub.infrastructure.config.IntegrationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisRateLimiter implements RateLimiter {

    private final StringRedisTemplate redis;
    private final Map<String, IntegrationProperties.RateLimit.Bucket> buckets;

    public RedisRateLimiter(StringRedisTemplate redis, IntegrationProperties props) {
        this.redis = redis;
        this.buckets = Map.of("shopify-webhook", props.rateLimit().shopifyWebhook());
    }

    @Override
    public Decision tryAcquire(String bucket) {
        var spec = buckets.get(bucket);
        if (spec == null) {
            return Decision.allow();
        }

        String key = "ratelimit:" + bucket;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            // TODO (Semana 3): mover INCR+EXPIRE para um script Lua para atomicidade real.
            redis.expire(key, spec.window());
        }

        if (count != null && count > spec.limit()) {
            Long ttlSeconds = redis.getExpire(key, TimeUnit.SECONDS);
            Duration retryAfter = (ttlSeconds == null || ttlSeconds < 0)
                    ? spec.window()
                    : Duration.ofSeconds(ttlSeconds);
            return Decision.deny(retryAfter);
        }
        return Decision.allow();
    }
}
