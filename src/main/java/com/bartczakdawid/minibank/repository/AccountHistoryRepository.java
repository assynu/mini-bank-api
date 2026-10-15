package com.bartczakdawid.minibank.repository;

import com.bartczakdawid.minibank.domain.AccountHistoryEntry;
import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.jooq.enums.AccountHistoryType;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static com.bartczakdawid.minibank.jooq.Tables.ACCOUNTS;
import static com.bartczakdawid.minibank.jooq.Tables.ACCOUNT_HISTORY;

public class AccountHistoryRepository {

    private final DSLContext dsl;

    public AccountHistoryRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(AccountHistoryEntry entry) {
        dsl.insertInto(ACCOUNT_HISTORY).set(ACCOUNT_HISTORY.ID, entry.id()).set(ACCOUNT_HISTORY.ACCOUNT_ID, entry.accountId()).set(ACCOUNT_HISTORY.TRANSFER_ID, entry.transferId()).set(ACCOUNT_HISTORY.TYPE, AccountHistoryType.valueOf(entry.type().name())).set(ACCOUNT_HISTORY.AMOUNT, entry.amount().amount()).execute();
    }

    public List<AccountHistoryEntry> findByAccountId(UUID accountId) {
        return dsl.select(ACCOUNT_HISTORY.asterisk(), ACCOUNTS.CURRENCY).from(ACCOUNT_HISTORY).join(ACCOUNTS).on(ACCOUNTS.ID.eq(ACCOUNT_HISTORY.ACCOUNT_ID)).where(ACCOUNT_HISTORY.ACCOUNT_ID.eq(accountId)).orderBy(ACCOUNT_HISTORY.CREATED_AT, ACCOUNT_HISTORY.ID).fetch(this::toEntry);
    }

    private AccountHistoryEntry toEntry(Record record) {
        var currency = Currency.getInstance(record.get(ACCOUNTS.CURRENCY));

        return new AccountHistoryEntry(record.get(ACCOUNT_HISTORY.ID), record.get(ACCOUNT_HISTORY.ACCOUNT_ID), record.get(ACCOUNT_HISTORY.TRANSFER_ID), AccountHistoryEntry.Type.valueOf(record.get(ACCOUNT_HISTORY.TYPE).name()), new Money(record.get(ACCOUNT_HISTORY.AMOUNT), currency));
    }
}