package com.williamfds.integrationhub.infrastructure.idempotency;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyStore {

    boolean tryRegister(String key, IdempotencyRecord record, Duration ttl);

    Optional<IdempotencyRecord> find(String key);

    void updateStatus(String key, IdempotencyStatus newStatus, Duration ttl);
}
