package com.bartczakdawid.minibank.api

import com.bartczakdawid.minibank.domain.Money
import com.bartczakdawid.minibank.domain.Transfer
import spock.lang.Specification
import tools.jackson.core.JacksonException

class TransferResponseSpec extends Specification {

    def json = Json.mapper()

    def "map transfer to response"() {
        given:
        def transfer = Transfer.create(UUID.randomUUID(), UUID.randomUUID(), Money.of("30", "PLN"))

        when:
        def response = TransferResponse.from(transfer)

        then:
        response.id() == transfer.id().toString()
        response.fromAccountId() == transfer.fromAccountId().toString()
        response.toAccountId() == transfer.toAccountId().toString()
        response.amount() == "30.00"
        response.currency() == "PLN"
    }

    def "read transfer request from json"() {
        given:
        def from = UUID.randomUUID().toString()
        def to = UUID.randomUUID().toString()
        def body = """{"fromAccountId":"$from","toAccountId":"$to","amount":"30.00","currency":"PLN"}"""

        when:
        def request = json.readValue(body, TransferRequest)

        then:
        request.fromAccountId() == from
        request.toAccountId() == to
        request.amount() == "30.00"
        request.currency() == "PLN"
    }

    def "reject transfer request with missing #field"() {
        when:
        json.readValue(body, TransferRequest)

        then:
        thrown(JacksonException)

        where:
        field           | body
        "fromAccountId" | '{"toAccountId":"b","amount":"30.00","currency":"PLN"}'
        "toAccountId"   | '{"fromAccountId":"a","amount":"30.00","currency":"PLN"}'
        "amount"        | '{"fromAccountId":"a","toAccountId":"b","currency":"PLN"}'
        "currency"      | '{"fromAccountId":"a","toAccountId":"b","amount":"30.00"}'
    }

    def "reject transfer request with unknown field"() {
        when:
        json.readValue('{"fromAccountId":"a","toAccountId":"b","amount":"30.00","currency":"PLN","fee":"1.00"}', TransferRequest)

        then:
        thrown(JacksonException)
    }
}