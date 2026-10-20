package com.bartczakdawid.minibank.repository

import com.bartczakdawid.minibank.IntegrationSpec
import com.bartczakdawid.minibank.domain.Account
import com.bartczakdawid.minibank.domain.AccountHistoryEntry
import com.bartczakdawid.minibank.domain.Money
import org.jooq.exception.DataAccessException

import java.time.Instant

class AccountHistoryRepositorySpec extends IntegrationSpec {

    def accounts = new AccountRepository(dsl)
    def history = new AccountHistoryRepository(dsl)

    def "insert and find deposit"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")
        accounts.insert(account)
        def entry = AccountHistoryEntry.deposit(account.id(), Money.of("100.00", "PLN"))

        when:
        history.insert(entry)

        then:
        history.findByAccountId(account.id()) == [entry]
    }

    def "find only entries of given account"() {
        given:
        def anna = Account.open("Anna", "PLN")
        def jan = Account.open("Jan", "PLN")
        accounts.insert(anna)
        accounts.insert(jan)
        history.insert(AccountHistoryEntry.deposit(anna.id(), Money.of("10.00", "PLN")))
        history.insert(AccountHistoryEntry.deposit(jan.id(), Money.of("20.00", "PLN")))

        expect:
        history.findByAccountId(anna.id())*.amount() == [Money.of("10.00", "PLN")]
    }

    def "find empty history"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")
        accounts.insert(account)

        expect:
        history.findByAccountId(account.id()).isEmpty()
    }

    def "reject entry for unknown account"() {
        when:
        history.insert(AccountHistoryEntry.deposit(UUID.randomUUID(), Money.of("1.00", "PLN")))

        then:
        thrown(DataAccessException)
    }

    def "find newest first"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")

        accounts.insert(account)

        def older = new AccountHistoryEntry(UUID.randomUUID(), account.id(), null, AccountHistoryEntry.Type.DEPOSIT, Money.of("10.00", "PLN"), Instant.parse("2026-01-01T10:00:00Z"))
        def newer = new AccountHistoryEntry(UUID.randomUUID(), account.id(), null, AccountHistoryEntry.Type.DEPOSIT, Money.of("20.00", "PLN"), Instant.parse("2026-01-01T11:00:00Z"))

        history.insert(older)
        history.insert(newer)

        expect:
        history.findByAccountId(account.id()) == [newer, older]
    }
}