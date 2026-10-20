package com.bartczakdawid.minibank.service

import com.bartczakdawid.minibank.IntegrationSpec
import com.bartczakdawid.minibank.domain.AccountHistoryEntry
import com.bartczakdawid.minibank.domain.CurrencyMismatchException
import com.bartczakdawid.minibank.domain.InsufficientFundsException
import com.bartczakdawid.minibank.domain.Money
import com.bartczakdawid.minibank.repository.AccountHistoryRepository
import com.bartczakdawid.minibank.repository.AccountRepository
import com.bartczakdawid.minibank.repository.TransferRepository

import static com.bartczakdawid.minibank.jooq.Tables.TRANSFERS

class TransferServiceSpec extends IntegrationSpec {

    def accountsRepository = new AccountRepository(dsl)
    def transfersRepository = new TransferRepository(dsl)
    def historyRepository = new AccountHistoryRepository(dsl)
    def accountService = new AccountService(accountsRepository, historyRepository, transactions)
    def transferService = new TransferService(accountsRepository, historyRepository, transfersRepository, transactions)

    def "transfer"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "PLN")
        accountService.deposit(anna.id(), Money.of("100.00", "PLN"))

        when:
        transferService.transfer(anna.id(), jan.id(), Money.of("30.00", "PLN"))

        then:
        accountService.get(anna.id()).balance() == Money.of("70.00", "PLN")
        accountService.get(jan.id()).balance() == Money.of("30.00", "PLN")
    }

    def "write two history entries"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "PLN")
        accountService.deposit(anna.id(), Money.of("100.00", "PLN"))

        when:
        def transfer = transferService.transfer(anna.id(), jan.id(), Money.of("30.00", "PLN"))

        then:
        def annaEntries = transferEntries(anna.id())
        def janEntries = transferEntries(jan.id())

        annaEntries.size() == 1
        janEntries.size() == 1

        annaEntries[0].transferId() == transfer.id()
        janEntries[0].transferId() == transfer.id()

        annaEntries[0].amount() == Money.of("-30.00", "PLN")
        janEntries[0].amount() == Money.of("30.00", "PLN")
    }

    def "balance equals sum of history"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "PLN")

        when:
        accountService.deposit(anna.id(), Money.of("100.00", "PLN"))
        accountService.deposit(jan.id(), Money.of("50.00", "PLN"))
        transferService.transfer(anna.id(), jan.id(), Money.of("30.00", "PLN"))
        transferService.transfer(jan.id(), anna.id(), Money.of("10.50", "PLN"))
        transferService.transfer(anna.id(), jan.id(), Money.of("0.01", "PLN"))

        then:
        accountService.get(anna.id()).balance() == historyTotal(anna.id())
        accountService.get(jan.id()).balance() == historyTotal(jan.id())
    }

    def "reject insufficient funds"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "PLN")
        accountService.deposit(anna.id(), Money.of("10.00", "PLN"))

        when:
        transferService.transfer(anna.id(), jan.id(), Money.of("30.00", "PLN"))

        then:
        thrown(InsufficientFundsException)
        accountService.get(anna.id()).balance() == Money.of("10.00", "PLN")
        accountService.get(jan.id()).balance() == Money.of("0.00", "PLN")
        dsl.fetchCount(TRANSFERS) == 0
        transferEntries(anna.id()).isEmpty()
        transferEntries(jan.id()).isEmpty()
    }

    def "reject unknown account"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        accountService.deposit(anna.id(), Money.of("100.00", "PLN"))

        when:
        transferService.transfer(anna.id(), UUID.randomUUID(), Money.of("30.00", "PLN"))

        then:
        thrown(AccountNotFoundException)
        accountService.get(anna.id()).balance() == Money.of("100.00", "PLN")
        dsl.fetchCount(TRANSFERS) == 0
    }

    def "reject currency mismatch"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "EUR")
        accountService.deposit(anna.id(), Money.of("100.00", "PLN"))

        when:
        transferService.transfer(anna.id(), jan.id(), Money.of("30.00", "PLN"))

        then:
        thrown(CurrencyMismatchException)
        accountService.get(anna.id()).balance() == Money.of("100.00", "PLN")
        accountService.get(jan.id()).balance() == Money.of("0.00", "EUR")
        dsl.fetchCount(TRANSFERS) == 0
        transferEntries(anna.id()).isEmpty()
    }

    def "get transfer"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "PLN")
        accountService.deposit(anna.id(), Money.of("100.00", "PLN"))
        def transfer = transferService.transfer(anna.id(), jan.id(), Money.of("30.00", "PLN"))

        expect:
        transferService.get(transfer.id()) == transfer
    }

    def "reject unknown transfer"() {
        when:
        transferService.get(UUID.randomUUID())

        then:
        thrown(TransferNotFoundException)
    }

    private List<AccountHistoryEntry> transferEntries(UUID accountId) {
        historyRepository.findByAccountId(accountId).findAll { it.type() == AccountHistoryEntry.Type.TRANSFER }
    }

    private Money historyTotal(UUID accountId) {
        historyRepository.findByAccountId(accountId)*.amount().inject(Money.of("0", "PLN")) { sum, amount -> sum.plus(amount) }
    }
}