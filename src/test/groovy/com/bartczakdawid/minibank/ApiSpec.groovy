package com.bartczakdawid.minibank

import com.bartczakdawid.minibank.api.AccountController
import com.bartczakdawid.minibank.api.ApiServer
import com.bartczakdawid.minibank.api.Json
import com.bartczakdawid.minibank.api.TransferController
import com.bartczakdawid.minibank.repository.AccountHistoryRepository
import com.bartczakdawid.minibank.repository.AccountRepository
import com.bartczakdawid.minibank.repository.TransferRepository
import com.bartczakdawid.minibank.service.AccountService
import com.bartczakdawid.minibank.service.TransferService
import spock.lang.Shared
import tools.jackson.databind.json.JsonMapper

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class ApiSpec extends IntegrationSpec {

    @Shared
    JsonMapper jsonMapper = Json.mapper()

    @Shared
    HttpClient client = HttpClient.newHttpClient()

    @Shared
    ApiServer server

    def setupSpec() {
        def accountRepository = new AccountRepository(dsl)
        def accountHistoryRepository = new AccountHistoryRepository(dsl)
        def accountService = new AccountService(accountRepository, accountHistoryRepository, transactions)

        def transferRepository = new TransferRepository(dsl)
        def transferService = new TransferService(accountRepository, accountHistoryRepository, transferRepository, transactions)

        def accountController = new AccountController(accountService, jsonMapper)
        def transferController = new TransferController(transferService, jsonMapper)

        server = new ApiServer(accountController, transferController, jsonMapper)
        server.start(0)
    }

    def cleanupSpec() {
        server?.stop()
    }

    HttpResponse<String> get(String path) {
        def request = HttpRequest.newBuilder(uri(path))
                .GET()
                .build()

        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    HttpResponse<String> post(String path, String body) {
        def request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()

        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private URI uri(String path) {
        URI.create("http://localhost:${server.port()}$path")
    }
}