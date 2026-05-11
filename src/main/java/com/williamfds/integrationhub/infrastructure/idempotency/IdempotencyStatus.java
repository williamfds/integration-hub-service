package com.williamfds.integrationhub.infrastructure.idempotency;

public enum IdempotencyStatus {
    RECEIVED,
    PUBLISHED,
    PROCESSED,
    FAILED
}
