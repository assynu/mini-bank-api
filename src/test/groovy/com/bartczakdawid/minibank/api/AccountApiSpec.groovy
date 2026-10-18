package com.bartczakdawid.minibank.api

import com.bartczakdawid.minibank.ApiSpec

class AccountApiSpec extends ApiSpec {

    def "open account"() {
        when:
        def response = post("/accounts", '{"owner":"Anna","currency":"PLN"}')

        then:
        response.statusCode() == 201

        and:
        def account = jsonMapper.readValue(response.body(), AccountResponse)
        account.owner() == "Anna"
        account.currency() == "PLN"
        account.balance() == "0.00"
        response.headers().firstValue("Location").get() == "/accounts/${account.id()}".toString()
        response.headers().firstValue("Content-Type").get().startsWith("application/json")
    }

    def "get account"() {
        given:
        def created = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)

        when:
        def response = get("/accounts/${created.id()}")

        then:
        response.statusCode() == 200
        jsonMapper.readValue(response.body(), AccountResponse) == created
    }

    def "deposit"() {
        given:
        def created = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)

        when:
        def response = post("/accounts/${created.id()}/deposits", '{"amount":"100.00","currency":"PLN"}')

        then:
        response.statusCode() == 200
        jsonMapper.readValue(response.body(), AccountResponse).balance() == "100.00"
    }

    def "reject unknown account"() {
        when:
        def response = get("/accounts/${UUID.randomUUID()}")

        then:
        response.statusCode() == 404
        jsonMapper.readValue(response.body(), ErrorResponse).error() == "ACCOUNT_NOT_FOUND"
    }

    def "reject invalid id"() {
        when:
        def response = get("/accounts/abc")

        then:
        response.statusCode() == 400
        jsonMapper.readValue(response.body(), ErrorResponse).error() == "INVALID_REQUEST"
    }

    def "reject deposit #description"() {
        given:
        def created = jsonMapper.readValue(post("/accounts", '{"owner":"Anna","currency":"PLN"}').body(), AccountResponse)

        when:
        def response = post("/accounts/${created.id()}/deposits", body)

        then:
        response.statusCode() == status
        jsonMapper.readValue(response.body(), ErrorResponse).error() == error

        and:
        jsonMapper.readValue(get("/accounts/${created.id()}").body(), AccountResponse).balance() == "0.00"

        where:
        description              | body                                     || status | error
        "of zero"                | '{"amount":"0.00","currency":"PLN"}'     || 422    | "INVALID_AMOUNT"
        "in different currency"  | '{"amount":"100.00","currency":"EUR"}'   || 422    | "CURRENCY_MISMATCH"
        "with too many decimals" | '{"amount":"10.001","currency":"PLN"}'   || 400    | "INVALID_REQUEST"
        "with non-numeric amount"| '{"amount":"abc","currency":"PLN"}'      || 400    | "INVALID_REQUEST"
    }

    def "reject malformed request: #description"() {
        when:
        def response = post("/accounts", body)

        then:
        response.statusCode() == 400
        jsonMapper.readValue(response.body(), ErrorResponse).error() == "MALFORMED_REQUEST"

        where:
        description     | body
        "missing field" | '{"owner":"Anna"}'
        "unknown field" | '{"owner":"Anna","currency":"PLN","balance":"1000"}'
        "not json"      | 'hello'
        "empty body"    | ''
    }

    def "reject unknown endpoint"() {
        when:
        def response = get("/nope")

        then:
        response.statusCode() == 404
        jsonMapper.readValue(response.body(), ErrorResponse).error() == "NOT_FOUND"
    }
}