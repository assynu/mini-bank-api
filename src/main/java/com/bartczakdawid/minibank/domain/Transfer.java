package com.bartczakdawid.minibank.domain;

import java.util.UUID;

public record Transfer(UUID id, String requestId, UUID fromAccountId, UUID toAccountId, Money amount) {
    public static final int MAX_REQUEST_ID_LENGTH = 255;

    public Transfer {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId cannot be blank");
        }

        if (requestId.length() > MAX_REQUEST_ID_LENGTH) {
            throw new IllegalArgumentException("requestId cannot be longer than " + MAX_REQUEST_ID_LENGTH);
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

    public static Transfer create(String requestId, UUID fromAccount, UUID toAccount, Money amount) {
        return new Transfer(UUID.randomUUID(), requestId, fromAccount, toAccount, amount);
    }

    public boolean sameRequestAs(UUID fromAccountId, UUID toAccountId, Money amount) {
        return this.fromAccountId.equals(fromAccountId)
                && this.toAccountId.equals(toAccountId)
                && this.amount.equals(amount);
    }
}
