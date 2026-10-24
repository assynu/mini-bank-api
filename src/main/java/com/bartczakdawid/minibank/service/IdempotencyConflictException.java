package com.bartczakdawid.minibank.service;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String requestId) {
        super("Request id " + requestId + " was already used for a different transfer");
    }
}