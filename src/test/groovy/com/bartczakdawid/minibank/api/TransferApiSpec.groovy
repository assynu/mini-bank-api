package com.bartczakdawid.minibank.api

import com.bartczakdawid.minibank.ApiSpec

class TransferApiSpec extends ApiSpec {

    def "create transfer"() {
        given:
        def a = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)
        def b = jsonMapper.readValue(post("/accounts", '{"owner":"Bob","currency":"PLN"}').body(), AccountResponse)
        post("/accounts/${a.id()}/deposits", '{"amount":"100.00","currency":"PLN"}')
        def requestId = UUID.randomUUID().toString()

        when:
        def response = post("/transfers",
                """{"requestId":"${requestId}","fromAccountId":"${a.id()}","toAccountId":"${b.id()}","amount":"30.00","currency":"PLN"}""")

        then:
        response.statusCode() == 201

        and:
        def transfer = jsonMapper.readValue(response.body(), TransferResponse)
        transfer.requestId() == requestId
        transfer.fromAccountId() == a.id()
        transfer.toAccountId() == b.id()
        transfer.amount() == "30.00"
        transfer.currency() == "PLN"
        response.headers().firstValue("Location").get() == "/transfers/${transfer.id()}".toString()
    }

    def "get transfer"() {
        given:
        def a = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)
        def b = jsonMapper.readValue(post("/accounts", '{"owner":"Bob","currency":"PLN"}').body(), AccountResponse)
        post("/accounts/${a.id()}/deposits", '{"amount":"100.00","currency":"PLN"}')
        def created = jsonMapper.readValue(post("/transfers",
                """{"requestId":"${UUID.randomUUID()}","fromAccountId":"${a.id()}","toAccountId":"${b.id()}","amount":"30.00","currency":"PLN"}""").body(),
                TransferResponse)

        when:
        def response = get("/transfers/${created.id()}")

        then:
        response.statusCode() == 200
        jsonMapper.readValue(response.body(), TransferResponse) == created
    }

    def "update balances"() {
        given:
        def a = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)
        def b = jsonMapper.readValue(post("/accounts", '{"owner":"Bob","currency":"PLN"}').body(), AccountResponse)
        post("/accounts/${a.id()}/deposits", '{"amount":"100.00","currency":"PLN"}')

        when:
        post("/transfers",
                """{"requestId":"${UUID.randomUUID()}","fromAccountId":"${a.id()}","toAccountId":"${b.id()}","amount":"30.00","currency":"PLN"}""")

        then:
        jsonMapper.readValue(get("/accounts/${a.id()}").body(), AccountResponse).balance() == "70.00"
        jsonMapper.readValue(get("/accounts/${b.id()}").body(), AccountResponse).balance() == "30.00"
    }

    def "record history"() {
        given:
        def a = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)
        def b = jsonMapper.readValue(post("/accounts", '{"owner":"Bob","currency":"PLN"}').body(), AccountResponse)
        post("/accounts/${a.id()}/deposits", '{"amount":"100.00","currency":"PLN"}')

        when:
        def transfer = jsonMapper.readValue(post("/transfers",
                """{"requestId":"${UUID.randomUUID()}","fromAccountId":"${a.id()}","toAccountId":"${b.id()}","amount":"30.00","currency":"PLN"}""").body(),
                TransferResponse)

        then:
        def historyA = jsonMapper.readValue(get("/accounts/${a.id()}/transactions").body(), AccountHistoryEntryResponse[])
        historyA*.amount() == ["-30.00", "100.00"]
        historyA*.type() == ["TRANSFER", "DEPOSIT"]
        historyA[0].transferId() == transfer.id()

        and:
        def historyB = jsonMapper.readValue(get("/accounts/${b.id()}/transactions").body(), AccountHistoryEntryResponse[])
        historyB*.amount() == ["30.00"]
        historyB[0].transferId() == transfer.id()
    }

    def "reject unknown transfer"() {
        when:
        def response = get("/transfers/${UUID.randomUUID()}")

        then:
        response.statusCode() == 404
        jsonMapper.readValue(response.body(), ErrorResponse).error() == "TRANSFER_NOT_FOUND"
    }

    def "reject invalid transfer id"() {
        when:
        def response = get("/transfers/abc")

        then:
        response.statusCode() == 400
        jsonMapper.readValue(response.body(), ErrorResponse).error() == "INVALID_REQUEST"
    }

    def "reject transfer #description"() {
        given:
        def a = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)
        def b = jsonMapper.readValue(post("/accounts", '{"owner":"Bob","currency":"PLN"}').body(), AccountResponse)
        post("/accounts/${a.id()}/deposits", '{"amount":"100.00","currency":"PLN"}')
        def to = [A: a.id(), B: b.id(), unknown: UUID.randomUUID().toString()][target]

        when:
        def response = post("/transfers",
                """{"requestId":"${UUID.randomUUID()}","fromAccountId":"${a.id()}","toAccountId":"${to}","amount":"${amount}","currency":"${currency}"}""")

        then:
        response.statusCode() == status
        jsonMapper.readValue(response.body(), ErrorResponse).error() == error

        and:
        jsonMapper.readValue(get("/accounts/${a.id()}").body(), AccountResponse).balance() == "100.00"
        jsonMapper.readValue(get("/accounts/${b.id()}").body(), AccountResponse).balance() == "0.00"

        where:
        description              | target    | amount    | currency || status | error
        "with insufficient funds"| "B"       | "1000.00" | "PLN"    || 422    | "INSUFFICIENT_FUNDS"
        "to same account"        | "A"       | "10.00"   | "PLN"    || 422    | "SAME_ACCOUNT_TRANSFER"
        "in different currency"  | "B"       | "10.00"   | "EUR"    || 422    | "CURRENCY_MISMATCH"
        "of zero"                | "B"       | "0.00"    | "PLN"    || 422    | "INVALID_AMOUNT"
        "to unknown account"     | "unknown" | "10.00"   | "PLN"    || 404    | "ACCOUNT_NOT_FOUND"
    }

    def "reject request id #description"() {
        given:
        def a = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)
        def b = jsonMapper.readValue(post("/accounts", '{"owner":"Bob","currency":"PLN"}').body(), AccountResponse)
        post("/accounts/${a.id()}/deposits", '{"amount":"100.00","currency":"PLN"}')
        def accounts = """"fromAccountId":"${a.id()}","toAccountId":"${b.id()}","amount":"10.00","currency":"PLN\""""

        when:
        def response = post("/transfers", "{${requestIdField}${accounts}}")

        then:
        response.statusCode() == 400
        jsonMapper.readValue(response.body(), ErrorResponse).error() == error

        and:
        jsonMapper.readValue(get("/accounts/${a.id()}").body(), AccountResponse).balance() == "100.00"

        where:
        description | requestIdField                       || error
        "missing"   | ''                                   || "MALFORMED_REQUEST"
        "blank"     | '"requestId":"   ",'                 || "INVALID_REQUEST"
        "too long"  | "\"requestId\":\"${'x' * 256}\","    || "INVALID_REQUEST"
    }
}