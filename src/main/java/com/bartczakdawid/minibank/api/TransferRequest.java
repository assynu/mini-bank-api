package com.bartczakdawid.minibank.api;

public record TransferRequest(String requestId, String fromAccountId, String toAccountId, String amount, String currency) {
    public TransferRequest {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId is required");
        }
        if (fromAccountId == null) {
            throw new IllegalArgumentException("fromAccountId is required");
        }
        if (toAccountId == null) {
            throw new IllegalArgumentException("toAccountId is required");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (currency == null) {
            throw new IllegalArgumentException("currency is required");
        }
    }
}