package com.bartczakdawid.minibank.domain

import spock.lang.Specification

class AccountSpec extends Specification {

    def "open with zero balance"() {
        when:
        def account = Account.open("Anna", "PLN")

        then:
        account.id() != null
        account.owner() == "Anna"
        account.balance() == Money.of("0.00", "PLN")
        account.currency() == Currency.getInstance("PLN")
    }

    def "reject null id"() {
        when:
        new Account(null, "Anna", Money.of("0.00", "PLN"))

        then:
        thrown(IllegalArgumentException)
    }

    def "reject blank owner: '#owner'"() {
        when:
        new Account(UUID.randomUUID(), owner, Money.of("0.00", "PLN"))

        then:
        thrown(IllegalArgumentException)

        where:
        owner << [null, "", "   "]
    }

    def "reject null balance"() {
        when:
        new Account(UUID.randomUUID(), "Anna", null)

        then:
        thrown(IllegalArgumentException)
    }

    def "reject negative balance"() {
        when:
        new Account(UUID.randomUUID(), "Anna", Money.of("-0.01", "PLN"))

        then:
        thrown(IllegalArgumentException)
    }

    def "credit"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        expect:
        account.credit(Money.of("50.00", "PLN")).balance() == Money.of("150.00", "PLN")
    }

    def "credit leaves original unchanged"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        when:
        account.credit(Money.of("50.00", "PLN"))

        then:
        account.balance() == Money.of("100.00", "PLN")
    }

    def "reject credit of #amount"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        when:
        account.credit(Money.of(amount, "PLN"))

        then:
        thrown(InvalidAmountException)

        where:
        amount << ["0.00", "-1.00"]
    }

    def "reject credit in different currency"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        when:
        account.credit(Money.of("50.00", "EUR"))

        then:
        thrown(CurrencyMismatchException)
    }

    def "debit"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        expect:
        account.debit(Money.of("30.00", "PLN")).balance() == Money.of("70.00", "PLN")
    }

    def "debit whole balance"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        expect:
        account.debit(Money.of("100.00", "PLN")).balance() == Money.of("0.00", "PLN")
    }

    def "reject debit beyond balance"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        when:
        account.debit(Money.of("100.01", "PLN"))

        then:
        thrown(InsufficientFundsException)
    }

    def "reject debit of #amount"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        when:
        account.debit(Money.of(amount, "PLN"))

        then:
        thrown(InvalidAmountException)

        where:
        amount << ["0.00", "-1.00"]
    }

    def "reject debit in different currency"() {
        given:
        def account = new Account(UUID.randomUUID(), "Anna", Money.of("100.00", "PLN"))

        when:
        account.debit(Money.of("200.00", "EUR"))

        then:
        thrown(CurrencyMismatchException)
    }
}