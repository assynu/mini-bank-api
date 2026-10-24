package com.bartczakdawid.minibank.api;

import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.domain.Transfer;
import com.bartczakdawid.minibank.service.TransferService;
import spark.Request;
import spark.Response;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

public class TransferController {
    private final TransferService transferService;
    private final JsonMapper jsonMapper;

    public TransferController(TransferService transferService, JsonMapper jsonMapper) {
        this.transferService = transferService;
        this.jsonMapper = jsonMapper;
    }

    public String create(Request request, Response response) {
        var body = jsonMapper.readValue(request.body(), TransferRequest.class);

        String requestId = body.requestId();
        UUID fromAccountId = UUID.fromString(body.fromAccountId());
        UUID toAccountId = UUID.fromString(body.toAccountId());
        Money amount = Money.of(body.amount(), body.currency());

        Transfer transfer = transferService.transfer(requestId, fromAccountId, toAccountId, amount);

        response.status(201);
        response.header("Location", "/transfers/" + transfer.id());

        return jsonMapper.writeValueAsString(TransferResponse.from(transfer));
    }

    public String get(Request request, Response ignoredResponse) {
        var transferId = transferId(request);
        Transfer transfer = transferService.get(transferId);

        return jsonMapper.writeValueAsString(TransferResponse.from(transfer));
    }

    private UUID transferId(Request request) {
        return UUID.fromString(request.params(":id"));
    }
}
