package com.bartczakdawid.minibank.repository

import com.bartczakdawid.minibank.IntegrationSpec
import com.bartczakdawid.minibank.domain.Account
import com.bartczakdawid.minibank.domain.Money
import org.jooq.exception.DataAccessException

class AccountRepositorySpec extends IntegrationSpec {

    def repository = new AccountRepository(dsl)

    def "insert and find"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")

        when:
        repository.insert(account)

        then:
        repository.findById(account.id()) == Optional.of(account)
    }

    def "find non-existent id"() {
        when:
        def found = repository.findById(UUID.randomUUID())

        then:
        found.isEmpty()
    }

    def "update balance"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")
        repository.insert(account)

        when:
        repository.update(account.credit(Money.of("100", "PLN")))

        then:
        repository.findById(account.id()).get().balance() == Money.of("100", "PLN")
    }

    def "reject update of missing account"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")

        when:
        repository.update(account)

        then:
        thrown(IllegalStateException)
    }

    def "keep scale for #currency"() {
        given:
        def account = Account.open("Jan Kowalski", currency).credit(Money.of(amount, currency))
        repository.insert(account)

        when:
        def balance = repository.findById(account.id()).get().balance().amount()

        then:
        balance == new BigDecimal(amount)
        balance.scale() == scale

        where:
        currency | amount   || scale
        "PLN"    | "100.00" || 2
        "JPY"    | "100"    || 0
    }

    def "reject duplicate id"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")
        repository.insert(account)

        when:
        repository.insert(account)

        then:
        thrown(DataAccessException)
    }

    def "insert and find for update"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")
        repository.insert(account)

        expect:
        repository.findByIdForUpdate(account.id()) == Optional.of(account)
    }

    def "find non-existent id for update"() {
        expect:
        repository.findByIdForUpdate(UUID.randomUUID()).isEmpty()
    }
}