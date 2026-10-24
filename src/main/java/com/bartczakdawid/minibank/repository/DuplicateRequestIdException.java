package com.bartczakdawid.minibank.repository;

public class DuplicateRequestIdException extends RuntimeException {
    public DuplicateRequestIdException(String requestId, Throwable cause) {
        super("Transfer with request id " + requestId + " already exists", cause);
    }
}