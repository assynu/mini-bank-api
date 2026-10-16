package com.bartczakdawid.minibank.domain

import spock.lang.Specification

class TransferSpec extends Specification {
    def "create transfer"() {
        given:
        def accountOne = UUID.randomUUID()
        def accountTwo = UUID.randomUUID()
        def amount = Money.of("50", "PLN")

        when:
        def transfer = Transfer.create(accountOne, accountTwo, amount)

        then:
        transfer.id() != null
        transfer.fromAccountId() == accountOne
        transfer.toAccountId() == accountTwo
        transfer.amount() == amount
    }

    def "reject null #field"() {
        when:
        new Transfer(id, from, to, amount)

        then:
        thrown(IllegalArgumentException)

        where:
        field    | id                | from              | to                | amount
        "id"     | null              | UUID.randomUUID() | UUID.randomUUID() | Money.of("1.00", "PLN")
        "from"   | UUID.randomUUID() | null              | UUID.randomUUID() | Money.of("1.00", "PLN")
        "to"     | UUID.randomUUID() | UUID.randomUUID() | null              | Money.of("1.00", "PLN")
        "amount" | UUID.randomUUID() | UUID.randomUUID() | UUID.randomUUID() | null
    }

    def "reject invalid amount: #amount"() {
        when:
        Transfer.create(UUID.randomUUID(), UUID.randomUUID(), Money.of(amount, "PLN"))

        then:
        thrown(InvalidAmountException)

        where:
        amount << ["-10", "0"]
    }

    def "reject transfer to same account"() {
        when:
        def account = UUID.randomUUID()
        Transfer.create(account, account, Money.of("20", "PLN"))

        then:
        thrown(SameAccountTransferException)
    }
}
