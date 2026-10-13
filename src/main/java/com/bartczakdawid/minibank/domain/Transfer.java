package com.bartczakdawid.minibank.domain;

import java.util.UUID;

public record Transfer(UUID id, UUID fromAccountId, UUID toAccountId, Money amount) {
    public Transfer {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (fromAccountId == null) {
            throw new IllegalArgumentException("fromAccountId cannot be null");
        }

        if (toAccountId == null) {
            throw new IllegalArgumentException("toAccountId cannot be null");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new SameAccountTransferException(fromAccountId);
        }

        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }

        if (!amount.isPositive()) {
            throw new InvalidAmountException(amount);
        }
    }

    public static Transfer create(UUID fromAccount, UUID toAccount, Money amount) {
        return new Transfer(UUID.randomUUID(), fromAccount, toAccount, amount);
    }
}
