package com.bartczakdawid.minibank.api;

import com.bartczakdawid.minibank.domain.AccountHistoryEntry;

public record AccountHistoryEntryResponse(String id, String type, String amount, String currency, String transferId, String createdAt) {
    public static AccountHistoryEntryResponse from(AccountHistoryEntry entry) {
        return new AccountHistoryEntryResponse(
                entry.id().toString(),
                entry.type().name(),
                entry.amount().amount().toPlainString(),
                entry.amount().currency().getCurrencyCode(),
                entry.transferId() == null ? null : entry.transferId().toString(),
                entry.createdAt().toString()
        );
    }
}