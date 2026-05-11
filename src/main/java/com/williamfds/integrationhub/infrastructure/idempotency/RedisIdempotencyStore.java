package com.williamfds.integrationhub.infrastructure.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisIdempotencyStore(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public boolean tryRegister(String key, IdempotencyRecord record, Duration ttl) {
        Boolean claimed = redis.opsForValue().setIfAbsent(key, serialize(record), ttl);
        return Boolean.TRUE.equals(claimed);
    }

    @Override
    public Optional<IdempotencyRecord> find(String key) {
        String raw = redis.opsForValue().get(key);
        return raw == null ? Optional.empty() : Optional.of(deserialize(raw));
    }

    @Override
    public void updateStatus(String key, IdempotencyStatus newStatus, Duration ttl) {
        find(key).ifPresent(existing ->
                redis.opsForValue().set(key, serialize(existing.withStatus(newStatus)), ttl));
    }

    private String serialize(IdempotencyRecord record) {
        try {
            return mapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotency record", e);
        }
    }

    private IdempotencyRecord deserialize(String raw) {
        try {
            return mapper.readValue(raw, IdempotencyRecord.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize idempotency record", e);
        }
    }
}
