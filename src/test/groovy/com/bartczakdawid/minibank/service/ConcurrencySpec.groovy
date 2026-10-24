package com.bartczakdawid.minibank.service

import com.bartczakdawid.minibank.IntegrationSpec
import com.bartczakdawid.minibank.domain.InsufficientFundsException
import com.bartczakdawid.minibank.domain.Money
import com.bartczakdawid.minibank.repository.AccountHistoryRepository
import com.bartczakdawid.minibank.repository.AccountRepository
import com.bartczakdawid.minibank.repository.TransferRepository
import spock.lang.Timeout

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

import static com.bartczakdawid.minibank.jooq.Tables.TRANSFERS

class ConcurrencySpec extends IntegrationSpec {

    def accounts = new AccountRepository(dsl)
    def history = new AccountHistoryRepository(dsl)
    def transfers = new TransferRepository(dsl)
    def accountService = new AccountService(accounts, history, transactions)
    def transferService = new TransferService(accounts, history, transfers, transactions)

    @Timeout(30)
    def "keep all concurrent deposits"() {
        given:
        def account = accountService.open("Anna", "PLN")
        def threads = 50
        def executor = Executors.newFixedThreadPool(threads)
        def start = new CountDownLatch(1)

        when:
        def futures = (1..threads).collect {
            executor.submit({
                start.await()
                accountService.deposit(account.id(), Money.of("10.00", "PLN"))
            } as Callable)
        }
        start.countDown()
        futures*.get()

        then:
        accountService.get(account.id()).balance() == Money.of("500.00", "PLN")
        history.findByAccountId(account.id()).size() == threads

        cleanup:
        executor.shutdown()
    }

    @Timeout(30)
    def "keep balances consistent under concurrent transfers"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "PLN")
        accountService.deposit(anna.id(), Money.of("100.00", "PLN"))
        def threads = 50
        def executor = Executors.newFixedThreadPool(threads)
        def start = new CountDownLatch(1)

        when:
        def futures = (1..threads).collect {
            executor.submit({
                start.await()
                try {
                    transferService.transfer(UUID.randomUUID().toString(), anna.id(), jan.id(), Money.of("10.00", "PLN"))
                    true
                } catch (InsufficientFundsException ignored) {
                    false
                }
            } as Callable<Boolean>)
        }
        start.countDown()
        def results = futures*.get()

        then:
        results.count { it } == 10
        results.count { !it } == 40
        accountService.get(anna.id()).balance() == Money.of("0.00", "PLN")
        accountService.get(jan.id()).balance() == Money.of("100.00", "PLN")
        dsl.fetchCount(TRANSFERS) == 10

        cleanup:
        executor.shutdown()
    }

    @Timeout(30)
    def "transfer both ways without deadlock"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "PLN")
        accountService.deposit(anna.id(), Money.of("1000.00", "PLN"))
        accountService.deposit(jan.id(), Money.of("1000.00", "PLN"))
        def threads = 50
        def executor = Executors.newFixedThreadPool(threads)
        def start = new CountDownLatch(1)

        when:
        def futures = (1..threads).collect { i ->
            def from = i % 2 == 0 ? anna : jan
            def to = i % 2 == 0 ? jan : anna
            executor.submit({
                start.await()
                transferService.transfer(UUID.randomUUID().toString(), from.id(), to.id(), Money.of("1.00", "PLN"))
            } as Callable)
        }
        start.countDown()
        futures*.get()

        then:
        accountService.get(anna.id()).balance() == Money.of("1000.00", "PLN")
        accountService.get(jan.id()).balance() == Money.of("1000.00", "PLN")
        dsl.fetchCount(TRANSFERS) == threads

        cleanup:
        executor.shutdown()
    }

    @Timeout(30)
    def "transfer in a cycle without deadlock"() {
        given:
        def anna = accountService.open("Anna", "PLN")
        def jan = accountService.open("Jan", "PLN")
        def ola = accountService.open("Ola", "PLN")
        [anna, jan, ola].each { accountService.deposit(it.id(), Money.of("1000.00", "PLN")) }
        def routes = [[anna, jan], [jan, ola], [ola, anna]]
        def threads = 60
        def executor = Executors.newFixedThreadPool(threads)
        def start = new CountDownLatch(1)

        when:
        def futures = (1..threads).collect { i ->
            def (from, to) = routes[i % 3]
            executor.submit({
                start.await()
                transferService.transfer(UUID.randomUUID().toString(), from.id(), to.id(), Money.of("1.00", "PLN"))
            } as Callable)
        }
        start.countDown()
        futures*.get()

        then:
        [anna, jan, ola].every { accountService.get(it.id()).balance() == Money.of("1000.00", "PLN") }
        dsl.fetchCount(TRANSFERS) == threads

        cleanup:
        executor.shutdown()
    }
}