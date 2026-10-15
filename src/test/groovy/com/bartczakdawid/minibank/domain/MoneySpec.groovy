package com.bartczakdawid.minibank.domain

import spock.lang.Specification

class MoneySpec extends Specification {

    def "create from amount and currency code"() {
        when:
        def money = Money.of("10.50", "PLN")

        then:
        money.amount() == new BigDecimal("10.50")
        money.currency() == Currency.getInstance("PLN")
    }

    def "reject null amount"() {
        when:
        new Money(null, Currency.getInstance("PLN"))

        then:
        thrown(IllegalArgumentException)
    }

    def "reject null currency"() {
        when:
        new Money(new BigDecimal("10.00"), null)

        then:
        thrown(IllegalArgumentException)
    }

    def "normalise #amount #currency to scale #scale"() {
        expect:
        Money.of(amount, currency).amount().scale() == scale

        where:
        amount  | currency || scale
        "10"    | "PLN"    || 2
        "10.0"  | "PLN"    || 2
        "10.00" | "PLN"    || 2
        "10"    | "JPY"    || 0
    }

    def "equal regardless of scale"() {
        expect:
        Money.of("10", "PLN") == Money.of("10.00", "PLN")
    }

    def "reject excess precision: #amount #currency"() {
        when:
        Money.of(amount, currency)

        then:
        thrown(IllegalArgumentException)

        where:
        amount   | currency
        "10.001" | "PLN"
        "10.5"   | "JPY"
    }

    def "add"() {
        expect:
        Money.of("10.50", "PLN").plus(Money.of("0.25", "PLN")) == Money.of("10.75", "PLN")
    }

    def "refuse to add different currencies"() {
        when:
        Money.of("10.00", "PLN").plus(Money.of("10.00", "EUR"))

        then:
        thrown(CurrencyMismatchException)
    }

    def "subtract below zero"() {
        expect:
        Money.of("10.00", "PLN").minus(Money.of("25.50", "PLN")) == Money.of("-15.50", "PLN")
    }

    def "refuse to subtract different currencies"() {
        when:
        Money.of("10.00", "PLN").minus(Money.of("10.00", "EUR"))

        then:
        thrown(CurrencyMismatchException)
    }

    def "sign of #amount"() {
        expect:
        Money.of(amount, "PLN").isPositive() == positive
        Money.of(amount, "PLN").isNegative() == negative

        where:
        amount  || positive | negative
        "10.00" || true     | false
        "0.00"  || false    | false
        "-0.01" || false    | true
    }

    def "negate amount"() {
        expect:
        Money.of("100", "PLN").negate().equals(Money.of("-100", "PLN"))
    }
}