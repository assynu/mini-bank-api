package com.bartczakdawid.minibank.domain;

public class InvalidAmountException extends DomainException {
    public InvalidAmountException(Money amount) {
        super("Invalid amount: " + amount);
    }
}
