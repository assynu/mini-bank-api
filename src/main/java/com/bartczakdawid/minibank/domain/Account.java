package com.bartczakdawid.minibank.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record Account(UUID id, String owner, Money balance) {
    public Account {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Owner cannot be null or empty");
        }

        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }

        if (balance.isNegative()) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
    }

    public static Account open(String owner, Currency currency) {
        return new Account(UUID.randomUUID(), owner, new Money(BigDecimal.ZERO, currency));
    }

    public Currency currency() {
        return balance.currency();
    }

    public Account credit(Money amount) {
        if (!amount.isPositive()) {
            throw new InvalidAmountException(amount);
        }

        return new Account(id, owner, balance.plus(amount));
    }

    public Account debit(Money amount) {
        if (!amount.isPositive()) {
            throw new InvalidAmountException(amount);
        }

        Money newBalance = balance.minus(amount);
        if (newBalance.isNegative()) {
            throw new InsufficientFundsException(balance, amount);
        }

        return new Account(id, owner, newBalance);
    }
}
