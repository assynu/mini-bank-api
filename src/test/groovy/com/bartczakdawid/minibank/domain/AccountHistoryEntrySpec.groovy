package com.bartczakdawid.minibank.domain

import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

class AccountHistoryEntrySpec extends Specification {

    def "deposit"() {
        given:
        def accountId = UUID.randomUUID()

        when:
        def entry = AccountHistoryEntry.deposit(accountId, Money.of("100.00", "PLN"))

        then:
        entry.id() != null
        entry.accountId() == accountId
        entry.transferId() == null
        entry.type() == AccountHistoryEntry.Type.DEPOSIT
        entry.amount() == Money.of("100.00", "PLN")
    }

    def "transfer entries"() {
        given:
        def transfer = Transfer.create(UUID.randomUUID(), UUID.randomUUID(), Money.of("200.00", "PLN"))

        when:
        def debit = AccountHistoryEntry.transferDebit(transfer)
        def credit = AccountHistoryEntry.transferCredit(transfer)

        then:
        debit.accountId() == transfer.fromAccountId()
        credit.accountId() == transfer.toAccountId()
        debit.transferId() == transfer.id()
        credit.transferId() == transfer.id()
        debit.type() == AccountHistoryEntry.Type.TRANSFER
        credit.type() == AccountHistoryEntry.Type.TRANSFER
        debit.amount() == Money.of("-200.00", "PLN")
        credit.amount() == Money.of("200.00", "PLN")
        debit.amount().plus(credit.amount()) == Money.of("0.00", "PLN")
    }

    def "reject zero amount"() {
        when:
        AccountHistoryEntry.deposit(UUID.randomUUID(), Money.of("0.00", "PLN"))

        then:
        thrown(IllegalArgumentException)
    }

    def "reject negative deposit"() {
        when:
        AccountHistoryEntry.deposit(UUID.randomUUID(), Money.of("-1.00", "PLN"))

        then:
        thrown(IllegalArgumentException)
    }

    def "reject deposit with transfer id"() {
        when:
        new AccountHistoryEntry(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                AccountHistoryEntry.Type.DEPOSIT, Money.of("1.00", "PLN"), Instant.now())

        then:
        thrown(IllegalArgumentException)
    }

    def "reject transfer without transfer id"() {
        when:
        new AccountHistoryEntry(UUID.randomUUID(), UUID.randomUUID(), null,
                AccountHistoryEntry.Type.TRANSFER, Money.of("1.00", "PLN"), Instant.now())

        then:
        thrown(IllegalArgumentException)
    }

    def "reject null #field"() {
        when:
        new AccountHistoryEntry(id, accountId, null, type, amount, Instant.now())

        then:
        thrown(IllegalArgumentException)

        where:
        field       | id                | accountId         | type                             | amount
        "id"        | null              | UUID.randomUUID() | AccountHistoryEntry.Type.DEPOSIT | Money.of("1.00", "PLN")
        "accountId" | UUID.randomUUID() | null              | AccountHistoryEntry.Type.DEPOSIT | Money.of("1.00", "PLN")
        "type"      | UUID.randomUUID() | UUID.randomUUID() | null                             | Money.of("1.00", "PLN")
        "amount"    | UUID.randomUUID() | UUID.randomUUID() | AccountHistoryEntry.Type.DEPOSIT | null
    }

    def "set createdAt"() {
        given:
        def before = Instant.now().truncatedTo(ChronoUnit.MICROS)

        when:
        def entry = AccountHistoryEntry.deposit(UUID.randomUUID(), Money.of("100.00", "PLN"))

        then:
        !entry.createdAt().isBefore(before)
        !entry.createdAt().isAfter(Instant.now())
        entry.createdAt() == entry.createdAt().truncatedTo(ChronoUnit.MICROS)
    }
}