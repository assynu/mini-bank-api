package com.bartczakdawid.minibank.domain;

public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
