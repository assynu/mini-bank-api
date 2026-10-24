package com.bartczakdawid.minibank.repository

import com.bartczakdawid.minibank.IntegrationSpec
import com.bartczakdawid.minibank.domain.Account
import com.bartczakdawid.minibank.domain.Money
import com.bartczakdawid.minibank.domain.Transfer
import org.jooq.exception.DataAccessException
import org.jooq.exception.IntegrityConstraintViolationException

class TransferRepositorySpec extends IntegrationSpec {

    def accounts = new AccountRepository(dsl)
    def transfers = new TransferRepository(dsl)

    def "insert and find"() {
        given:
        def anna = Account.open("Anna", "PLN")
        def jan = Account.open("Jan", "PLN")
        accounts.insert(anna)
        accounts.insert(jan)
        def transfer = Transfer.create(UUID.randomUUID().toString(), anna.id(), jan.id(), Money.of("50.00", "PLN"))

        when:
        transfers.insert(transfer)

        then:
        transfers.findById(transfer.id()) == Optional.of(transfer)
    }

    def "find non-existent id"() {
        expect:
        transfers.findById(UUID.randomUUID()).isEmpty()
    }

    def "reject transfer between unknown accounts"() {
        when:
        transfers.insert(Transfer.create(UUID.randomUUID().toString(), UUID.randomUUID(), UUID.randomUUID(), Money.of("50.00", "PLN")))

        then:
        thrown(DataAccessException)
    }

    def "find by request id"() {
        given:
        def anna = Account.open("Anna", "PLN")
        def jan = Account.open("Jan", "PLN")
        accounts.insert(anna)
        accounts.insert(jan)
        def transfer = Transfer.create("r-1", anna.id(), jan.id(), Money.of("50.00", "PLN"))
        transfers.insert(transfer)

        expect:
        transfers.findByRequestId("r-1") == Optional.of(transfer)
    }

    def "find non-existent request id"() {
        expect:
        transfers.findByRequestId("missing").isEmpty()
    }

    def "reject duplicate request id"() {
        given:
        def anna = Account.open("Anna", "PLN")
        def jan = Account.open("Jan", "PLN")
        accounts.insert(anna)
        accounts.insert(jan)
        transfers.insert(Transfer.create("r-1", anna.id(), jan.id(), Money.of("50.00", "PLN")))

        when:
        transfers.insert(Transfer.create("r-1", anna.id(), jan.id(), Money.of("50.00", "PLN")))

        then:
        thrown(IntegrityConstraintViolationException)
    }
}