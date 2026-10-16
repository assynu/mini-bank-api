package com.bartczakdawid.minibank.domain;

public abstract sealed class DomainException extends RuntimeException permits CurrencyMismatchException, InsufficientFundsException, InvalidAmountException, SameAccountTransferException {

    public DomainException(String message) {
        super(message);
    }
}