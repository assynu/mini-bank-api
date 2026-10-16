package com.bartczakdawid.minibank.domain;

public final class InvalidAmountException extends DomainException {
    public InvalidAmountException(Money amount) {
        super("Invalid amount: " + amount);
    }
}
