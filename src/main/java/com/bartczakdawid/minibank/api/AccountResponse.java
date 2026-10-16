package com.bartczakdawid.minibank.api;

import com.bartczakdawid.minibank.domain.Account;

public record AccountResponse(String id, String owner, String currency, String balance) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.id().toString(), account.owner(), account.currency().getCurrencyCode(), account.balance().amount().toPlainString());
    }
}
