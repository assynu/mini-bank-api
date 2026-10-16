package com.bartczakdawid.minibank.api

import com.bartczakdawid.minibank.domain.Account
import com.bartczakdawid.minibank.domain.Money
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class AccountResponseSpec extends Specification {

    def json = JsonMapper.builder().build()

    def "map account to response"() {
        given:
        def account = Account.open("Anna", "PLN").credit(Money.of("100", "PLN"))

        when:
        def response = AccountResponse.from(account)

        then:
        response.id() == account.id().toString()
        response.owner() == "Anna"
        response.currency() == "PLN"
        response.balance() == "100.00"
    }

    def "keep currency scale in balance"() {
        given:
        def account = Account.open("Anna", "JPY")

        expect:
        AccountResponse.from(account).balance() == "0"
    }

    def "read create account request from json"() {
        when:
        def request = json.readValue('{"owner":"Anna","currency":"PLN"}', CreateAccountRequest)

        then:
        request.owner() == "Anna"
        request.currency() == "PLN"
    }

    def "write balance as string"() {
        given:
        def account = Account.open("Anna", "PLN").credit(Money.of("100", "PLN"))

        when:
        def written = json.writeValueAsString(AccountResponse.from(account))

        then:
        written.contains('"balance":"100.00"')
    }
}