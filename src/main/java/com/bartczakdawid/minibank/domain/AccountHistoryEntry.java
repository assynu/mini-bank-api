package com.bartczakdawid.minibank.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record AccountHistoryEntry(UUID id, UUID accountId, UUID transferId, Type type, Money amount,
                                  Instant createdAt) {

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

        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
    }

    public static AccountHistoryEntry deposit(UUID accountId, Money amount) {
        return new AccountHistoryEntry(UUID.randomUUID(), accountId, null, Type.DEPOSIT, amount, now());
    }

    public static AccountHistoryEntry transferDebit(Transfer transfer) {
        return new AccountHistoryEntry(
                UUID.randomUUID(),
                transfer.fromAccountId(),
                transfer.id(),
                Type.TRANSFER,
                transfer.amount().negate(),
                now()
        );
    }

    public static AccountHistoryEntry transferCredit(Transfer transfer) {
        return new AccountHistoryEntry(
                UUID.randomUUID(),
                transfer.toAccountId(),
                transfer.id(),
                Type.TRANSFER,
                transfer.amount(),
                now()
        );
    }

    private static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public enum Type {DEPOSIT, TRANSFER}
}