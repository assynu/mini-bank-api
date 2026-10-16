package com.bartczakdawid.minibank.domain;

import java.util.UUID;

public record AccountHistoryEntry(UUID id, UUID accountId, UUID transferId, Type type, Money amount) {

    public AccountHistoryEntry {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        if (accountId == null) {
            throw new IllegalArgumentException("Account id cannot be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (!amount.isNegative() && !amount.isPositive()) {
            throw new IllegalArgumentException("Amount cannot be zero");
        }

        if (type == Type.DEPOSIT && transferId != null) {
            throw new IllegalArgumentException("Deposit cannot have a transfer id");
        }

        if (type == Type.DEPOSIT && amount.isNegative()) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        if (type == Type.TRANSFER && transferId == null) {
            throw new IllegalArgumentException("Transfer entry must have a transfer id");
        }
    }

    public static AccountHistoryEntry deposit(UUID accountId, Money amount) {
        return new AccountHistoryEntry(UUID.randomUUID(), accountId, null, Type.DEPOSIT, amount);
    }

    public static AccountHistoryEntry transferDebit(Transfer transfer) {
        return new AccountHistoryEntry(
                UUID.randomUUID(),
                transfer.fromAccountId(),
                transfer.id(),
                Type.TRANSFER,
                transfer.amount().negate()
        );
    }

    public static AccountHistoryEntry transferCredit(Transfer transfer) {
        return new AccountHistoryEntry(
                UUID.randomUUID(),
                transfer.toAccountId(),
                transfer.id(),
                Type.TRANSFER,
                transfer.amount()
        );
    }

    public enum Type {DEPOSIT, TRANSFER}
}