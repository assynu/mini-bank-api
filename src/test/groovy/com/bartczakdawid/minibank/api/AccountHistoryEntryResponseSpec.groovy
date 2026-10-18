package com.bartczakdawid.minibank.api

import com.bartczakdawid.minibank.domain.AccountHistoryEntry
import com.bartczakdawid.minibank.domain.Money
import com.bartczakdawid.minibank.domain.Transfer
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

import java.time.Instant

class AccountHistoryEntryResponseSpec extends Specification {

    def json = JsonMapper.builder().build()

    def "map deposit"() {
        given:
        def entry = AccountHistoryEntry.deposit(UUID.randomUUID(), Money.of("100", "PLN"))

        when:
        def response = AccountHistoryEntryResponse.from(entry)

        then:
        response.id() == entry.id().toString()
        response.type() == "DEPOSIT"
        response.amount() == "100.00"
        response.currency() == "PLN"
        response.transferId() == null
    }

    def "map transfer debit"() {
        given:
        def transfer = Transfer.create(UUID.randomUUID(), UUID.randomUUID(), Money.of("200.00", "PLN"))
        def entry = AccountHistoryEntry.transferDebit(transfer)

        when:
        def response = AccountHistoryEntryResponse.from(entry)

        then:
        response.type() == "TRANSFER"
        response.amount() == "-200.00"
        response.transferId() == transfer.id().toString()
    }

    def "map createdAt as ISO-8601"() {
        given:
        def entry = new AccountHistoryEntry(UUID.randomUUID(), UUID.randomUUID(), null,
                AccountHistoryEntry.Type.DEPOSIT, Money.of("1.00", "PLN"), Instant.parse("2026-10-18T12:00:00Z"))

        expect:
        AccountHistoryEntryResponse.from(entry).createdAt() == "2026-10-18T12:00:00Z"
    }

    def "write deposit transferId as null"() {
        given:
        def entry = AccountHistoryEntry.deposit(UUID.randomUUID(), Money.of("100", "PLN"))

        when:
        def written = json.writeValueAsString(AccountHistoryEntryResponse.from(entry))

        then:
        written.contains('"transferId":null')
        written.contains('"amount":"100.00"')
    }
}