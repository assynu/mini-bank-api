package com.bartczakdawid.minibank.repository;

import com.bartczakdawid.minibank.domain.Account;
import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.jooq.tables.records.AccountsRecord;
import org.jooq.DSLContext;

import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static com.bartczakdawid.minibank.jooq.tables.Accounts.ACCOUNTS;

public class AccountRepository {
    private final DSLContext dsl;

    public AccountRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(Account account) {
        dsl.insertInto(ACCOUNTS)
                .set(ACCOUNTS.ID, account.id())
                .set(ACCOUNTS.OWNER, account.owner())
                .set(ACCOUNTS.BALANCE, account.balance().amount())
                .set(ACCOUNTS.CURRENCY, account.currency().getCurrencyCode())
                .execute();
    }

    public void update(Account account) {
        var updated = dsl.update(ACCOUNTS)
                .set(ACCOUNTS.BALANCE, account.balance().amount())
                .where(ACCOUNTS.ID.eq(account.id()))
                .execute();

        if (updated < 1) {
            throw new IllegalStateException("Account with id " + account.id() + " does not exist");
        }
    }

    public Optional<Account> findById(UUID id) {
        AccountsRecord record = dsl.selectFrom(ACCOUNTS)
                .where(ACCOUNTS.ID.eq(id))
                .fetchOne();

        return Optional.ofNullable(record).map(this::toAccount);
    }

    private Account toAccount(AccountsRecord record) {
        var currency = Currency.getInstance(record.getCurrency());
        var balance = new Money(record.getBalance(), currency);

        return new Account(record.getId(), record.getOwner(), balance);
    }

    public Optional<Account> findByIdForUpdate(UUID id) {
        AccountsRecord record = dsl.selectFrom(ACCOUNTS)
                .where(ACCOUNTS.ID.eq(id))
                .forUpdate()
                .fetchOne();

        return Optional.ofNullable(record).map(this::toAccount);
    }
}
