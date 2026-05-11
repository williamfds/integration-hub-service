package com.williamfds.integrationhub.domain.exception;

public class NonRecoverableException extends RuntimeException {
    public NonRecoverableException(String message) {
        super(message);
    }

    public NonRecoverableException(String message, Throwable cause) {
        super(message, cause);
    }
}
