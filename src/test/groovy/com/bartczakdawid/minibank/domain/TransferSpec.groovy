package com.bartczakdawid.minibank.domain

import spock.lang.Specification

class TransferSpec extends Specification {
    def "create transfer"() {
        given:
        def requestId = UUID.randomUUID().toString()
        def accountOne = UUID.randomUUID()
        def accountTwo = UUID.randomUUID()
        def amount = Money.of("50", "PLN")

        when:
        def transfer = Transfer.create(requestId, accountOne, accountTwo, amount)

        then:
        transfer.id() != null
        transfer.requestId() == requestId
        transfer.fromAccountId() == accountOne
        transfer.toAccountId() == accountTwo
        transfer.amount() == amount
    }

    def "reject null #field"() {
        when:
        new Transfer(id, requestId, from, to, amount)

        then:
        thrown(IllegalArgumentException)

        where:
        field       | id                | requestId | from              | to                | amount
        "id"        | null              | "r-1"     | UUID.randomUUID() | UUID.randomUUID() | Money.of("1.00", "PLN")
        "requestId" | UUID.randomUUID() | null      | UUID.randomUUID() | UUID.randomUUID() | Money.of("1.00", "PLN")
        "from"      | UUID.randomUUID() | "r-1"     | null              | UUID.randomUUID() | Money.of("1.00", "PLN")
        "to"        | UUID.randomUUID() | "r-1"     | UUID.randomUUID() | null              | Money.of("1.00", "PLN")
        "amount"    | UUID.randomUUID() | "r-1"     | UUID.randomUUID() | UUID.randomUUID() | null
    }

    def "reject requestId #description"() {
        when:
        Transfer.create(requestId, UUID.randomUUID(), UUID.randomUUID(), Money.of("1.00", "PLN"))

        then:
        thrown(IllegalArgumentException)

        where:
        description | requestId
        "empty"     | ""
        "blank"     | "   "
        "too long"  | "x" * (Transfer.MAX_REQUEST_ID_LENGTH + 1)
    }

    def "accept requestId of max length"() {
        when:
        def transfer = Transfer.create("x" * Transfer.MAX_REQUEST_ID_LENGTH, UUID.randomUUID(), UUID.randomUUID(), Money.of("1.00", "PLN"))

        then:
        transfer.requestId().length() == Transfer.MAX_REQUEST_ID_LENGTH
    }

    def "reject invalid amount: #amount"() {
        when:
        Transfer.create(UUID.randomUUID().toString(), UUID.randomUUID(), UUID.randomUUID(), Money.of(amount, "PLN"))

        then:
        thrown(InvalidAmountException)

        where:
        amount << ["-10", "0"]
    }

    def "reject transfer to same account"() {
        when:
        def account = UUID.randomUUID()
        Transfer.create(UUID.randomUUID().toString(), account, account, Money.of("20", "PLN"))

        then:
        thrown(SameAccountTransferException)
    }

    def "same request"() {
        given:
        def from = UUID.randomUUID()
        def to = UUID.randomUUID()
        def transfer = Transfer.create("r-1", from, to, Money.of("30.00", "PLN"))

        expect:
        transfer.sameRequestAs(from, to, Money.of("30", "PLN"))
    }

    def "different request when #field differs"() {
        given:
        def from = UUID.randomUUID()
        def to = UUID.randomUUID()
        def transfer = Transfer.create("r-1", from, to, Money.of("30.00", "PLN"))

        expect:
        !transfer.sameRequestAs(
                field == "from" ? UUID.randomUUID() : from,
                field == "to" ? UUID.randomUUID() : to,
                field == "amount" ? Money.of("31.00", "PLN") : field == "currency" ? Money.of("30.00", "EUR") : Money.of("30.00", "PLN"))

        where:
        field << ["from", "to", "amount", "currency"]
    }
}