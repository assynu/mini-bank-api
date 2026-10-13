package com.bartczakdawid.minibank.domain;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(Money amount) {
        super("Invalid amount: " + amount);
    }
}
