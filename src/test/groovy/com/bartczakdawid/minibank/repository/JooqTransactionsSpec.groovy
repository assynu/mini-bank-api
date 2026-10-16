package com.bartczakdawid.minibank.repository

import com.bartczakdawid.minibank.IntegrationSpec
import com.bartczakdawid.minibank.domain.Account

class JooqTransactionsSpec extends IntegrationSpec {

    def accounts = new AccountRepository(dsl)

    def "commit on success"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")

        when:
        transactions.inTransaction(() -> {
            accounts.insert(account)
            return null
        })

        then:
        accounts.findById(account.id()).isPresent()
    }

    def "roll back on exception"() {
        given:
        def account = Account.open("Jan Kowalski", "PLN")

        when:
        transactions.inTransaction(() -> {
            accounts.insert(account)
            throw new IllegalStateException("boom")
        })

        then:
        thrown(IllegalStateException)
        accounts.findById(account.id()).isEmpty()
    }
}