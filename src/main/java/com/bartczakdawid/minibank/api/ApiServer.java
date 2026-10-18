package com.bartczakdawid.minibank.api;

import com.bartczakdawid.minibank.domain.*;
import com.bartczakdawid.minibank.service.AccountNotFoundException;
import com.bartczakdawid.minibank.service.TransferNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;
import spark.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class ApiServer {
    private static final Logger log = LoggerFactory.getLogger(ApiServer.class);

    private final AccountController accountController;
    private final TransferController transferController;
    private final JsonMapper json;
    private Service http;

    public ApiServer(AccountController accountController, TransferController transferController, JsonMapper json) {
        this.accountController = accountController;
        this.transferController = transferController;
        this.json = json;
    }

    public void start(int port) {
        http = Service.ignite().port(port);

        http.post("/accounts", accountController::create);
        http.post("/accounts/:id/deposits", accountController::deposit);
        http.post("/transfers", transferController::create);

        http.get("/accounts/:id", accountController::get);
        http.get("/transfers/:id", transferController::get);

        http.after((request, response) -> response.type("application/json"));

        http.exception(AccountNotFoundException.class, (exception, request, response) -> {
            error(response, 404, "ACCOUNT_NOT_FOUND", exception.getMessage());
        });

        http.exception(DomainException.class, (exception, request, response) -> {
            error(response, 422, code(exception), exception.getMessage());
        });

        http.exception(JacksonException.class, (exception, request, response) -> {
            error(response, 400, "MALFORMED_REQUEST", "Malformed or incomplete JSON body");
        });

        http.exception(IllegalArgumentException.class, (exception, request, response) -> {
            error(response, 400, "INVALID_REQUEST", exception.getMessage());
        });

        http.exception(Exception.class, (exception, request, response) -> {
            log.error("Unhandled error", exception);
            error(response, 500, "INTERNAL_ERROR", "Unexpected error");
        });

        http.exception(TransferNotFoundException.class, (exception, request, response) -> {
            error(response, 404, "TRANSFER_NOT_FOUND", exception.getMessage());
        });

        http.notFound((request, response) -> {
            response.type("application/json");
            return json.writeValueAsString(new ErrorResponse("NOT_FOUND", "No such endpoint"));
        });

        http.awaitInitialization();
    }

    public int port() {
        return http.port();
    }

    public void stop() {
        http.stop();
        http.awaitStop();
    }

    private void error(Response response, int status, String code, String message) {
        response.status(status);
        response.type("application/json");
        response.body(json.writeValueAsString(new ErrorResponse(code, message)));
    }

    private static String code(DomainException exception) {
        return switch (exception) {
            case CurrencyMismatchException ignored -> "CURRENCY_MISMATCH";
            case InsufficientFundsException ignored -> "INSUFFICIENT_FUNDS";
            case InvalidAmountException ignored -> "INVALID_AMOUNT";
            case SameAccountTransferException ignored -> "SAME_ACCOUNT_TRANSFER";
        };
    }
}