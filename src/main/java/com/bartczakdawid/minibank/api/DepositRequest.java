package com.bartczakdawid.minibank.api;

public record DepositRequest(String amount, String currency) {

    public DepositRequest {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (currency == null) {
            throw new IllegalArgumentException("currency is required");
        }
    }
}