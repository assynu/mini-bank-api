package com.bartczakdawid.minibank.repository;

import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.domain.Transfer;
import com.bartczakdawid.minibank.jooq.tables.records.TransfersRecord;
import org.jooq.DSLContext;
import org.jooq.exception.IntegrityConstraintViolationException;

import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static com.bartczakdawid.minibank.jooq.Tables.TRANSFERS;

public class TransferRepository {
    /* 23505 is the error code for duplicate */
    private static final String UNIQUE_VIOLATION = "23505";
    private final DSLContext dsl;

    public TransferRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(Transfer transfer) {
        try {
            dsl.insertInto(TRANSFERS)
                    .set(TRANSFERS.ID, transfer.id())
                    .set(TRANSFERS.REQUEST_ID, transfer.requestId())
                    .set(TRANSFERS.FROM_ACCOUNT_ID, transfer.fromAccountId())
                    .set(TRANSFERS.TO_ACCOUNT_ID, transfer.toAccountId())
                    .set(TRANSFERS.AMOUNT, transfer.amount().amount())
                    .set(TRANSFERS.CURRENCY, transfer.amount().currency().getCurrencyCode())
                    .execute();
        } catch (IntegrityConstraintViolationException exception) {
            if (UNIQUE_VIOLATION.equals(exception.sqlState())) {
                throw new DuplicateRequestIdException(transfer.requestId(), exception);
            }
            throw exception;
        }
    }

    public Optional<Transfer> findById(UUID id) {
        return dsl.selectFrom(TRANSFERS).where(TRANSFERS.ID.eq(id)).fetchOptional().map(this::toTransfer);
    }

    private Transfer toTransfer(TransfersRecord record) {
        var currency = Currency.getInstance(record.getCurrency());
        var amount = new Money(record.getAmount(), currency);

        return new Transfer(record.getId(), record.getRequestId(), record.getFromAccountId(), record.getToAccountId(), amount);
    }

    public Optional<Transfer> findByRequestId(String requestId) {
        return dsl.selectFrom(TRANSFERS)
                .where(TRANSFERS.REQUEST_ID.eq(requestId))
                .fetchOptional()
                .map(this::toTransfer);
    }
}