package com.bartczakdawid.minibank.api;

import com.bartczakdawid.minibank.domain.Account;
import com.bartczakdawid.minibank.domain.AccountHistoryEntry;
import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.service.AccountService;
import spark.Request;
import spark.Response;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

public class AccountController {
    private final AccountService accountService;
    private final JsonMapper jsonMapper;

    public AccountController(AccountService accountService, JsonMapper json) {
        this.accountService = accountService;
        this.jsonMapper = json;
    }

    public String create(Request request, Response response) {
        var body = jsonMapper.readValue(request.body(), CreateAccountRequest.class);

        Account account = accountService.open(body.owner(), body.currency());

        response.status(201);
        response.header("Location", "/accounts/" + account.id());

        return jsonMapper.writeValueAsString(AccountResponse.from(account));
    }

    public String get(Request request, Response ignoredResponse) {
        UUID accountId = accountId(request);
        Account account = accountService.get(accountId);

        return jsonMapper.writeValueAsString(AccountResponse.from(account));
    }

    public String transactions(Request request, Response ignoredResponse) {
        UUID accountId = accountId(request);
        List<AccountHistoryEntry> history = accountService.history(accountId);
        List<AccountHistoryEntryResponse> historyResponse = history.stream().map(AccountHistoryEntryResponse::from).toList();

        return jsonMapper.writeValueAsString(historyResponse);
    }

    public String deposit(Request request, Response ignoredResponse) {
        var body = jsonMapper.readValue(request.body(), DepositRequest.class);

        UUID accountId = accountId(request);
        Account account = accountService.deposit(accountId, Money.of(body.amount(), body.currency()));

        return jsonMapper.writeValueAsString(AccountResponse.from(account));
    }

    private UUID accountId(Request request) {
        return UUID.fromString(request.params(":id"));
    }
}
