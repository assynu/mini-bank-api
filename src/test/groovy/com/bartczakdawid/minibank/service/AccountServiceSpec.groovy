package com.bartczakdawid.minibank.service

import com.bartczakdawid.minibank.IntegrationSpec
import com.bartczakdawid.minibank.domain.AccountHistoryEntry
import com.bartczakdawid.minibank.domain.CurrencyMismatchException
import com.bartczakdawid.minibank.domain.InvalidAmountException
import com.bartczakdawid.minibank.domain.Money
import com.bartczakdawid.minibank.repository.AccountHistoryRepository
import com.bartczakdawid.minibank.repository.AccountRepository

class AccountServiceSpec extends IntegrationSpec {

    def accounts = new AccountRepository(dsl)
    def history = new AccountHistoryRepository(dsl)
    def service = new AccountService(accounts, history, transactions)

    def "open account"() {
        when:
        def account = service.open("Jan Kowalski", "PLN")

        then:
        service.get(account.id()) == account
        account.balance() == Money.of("0.00", "PLN")
    }

    def "reject unknown account"() {
        when:
        service.get(UUID.randomUUID())

        then:
        thrown(AccountNotFoundException)
    }

    def "deposit"() {
        given:
        def account = service.open("Jan Kowalski", "PLN")

        when:
        def result = service.deposit(account.id(), Money.of("100.00", "PLN"))

        then:
        result.balance() == Money.of("100.00", "PLN")
        service.get(account.id()).balance() == Money.of("100.00", "PLN")

        and:
        def entries = history.findByAccountId(account.id())
        entries.size() == 1
        entries[0].type() == AccountHistoryEntry.Type.DEPOSIT
        entries[0].amount() == Money.of("100.00", "PLN")
    }

    def "reject deposit to unknown account"() {
        when:
        service.deposit(UUID.randomUUID(), Money.of("100.00", "PLN"))

        then:
        thrown(AccountNotFoundException)
    }

    def "reject deposit of #amount #currency"() {
        given:
        def account = service.open("Jan Kowalski", "PLN")

        when:
        service.deposit(account.id(), Money.of(amount, currency))

        then:
        thrown(exception)
        service.get(account.id()).balance() == Money.of("0.00", "PLN")
        history.findByAccountId(account.id()).isEmpty()

        where:
        amount   | currency || exception
        "0.00"   | "PLN"    || InvalidAmountException
        "-1.00"  | "PLN"    || InvalidAmountException
        "100.00" | "EUR"    || CurrencyMismatchException
    }
}