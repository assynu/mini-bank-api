package com.bartczakdawid.minibank.api;

import com.bartczakdawid.minibank.domain.Transfer;

public record TransferResponse(String id, String requestId, String fromAccountId, String toAccountId, String amount, String currency) {
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.id().toString(),
                transfer.requestId(),
                transfer.fromAccountId().toString(),
                transfer.toAccountId().toString(),
                transfer.amount().amount().toPlainString(),
                transfer.amount().currency().getCurrencyCode()
        );
    }
}