package com.bartczakdawid.minibank.domain;

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException(Money balance, Money amount) {
        super("Insufficient funds, " + amount + " cannot be removed from balance: " + balance);
    }
}
