package com.bartczakdawid.minibank.api;

public record CreateAccountRequest(String owner, String currency) {

    public CreateAccountRequest {
        if (owner == null) {
            throw new IllegalArgumentException("owner is required");
        }
        if (currency == null) {
            throw new IllegalArgumentException("currency is required");
        }
    }
}