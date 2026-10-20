package com.bartczakdawid.minibank.service;

import com.bartczakdawid.minibank.domain.Account;
import com.bartczakdawid.minibank.domain.AccountHistoryEntry;
import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.repository.AccountHistoryRepository;
import com.bartczakdawid.minibank.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final Transactions transactions;

    public AccountService(AccountRepository accountRepository, AccountHistoryRepository accountHistoryRepository, Transactions transactions) {
        this.accountRepository = accountRepository;
        this.accountHistoryRepository = accountHistoryRepository;
        this.transactions = transactions;
    }

    public Account open(String owner, String currencyCode) {
        Account account = Account.open(owner, currencyCode);
        accountRepository.insert(account);

        log.atInfo()
                .addKeyValue("accountId", account.id())
                .addKeyValue("currency", account.currency())
                .log("Account opened");

        return account;
    }

    public Account get(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public List<AccountHistoryEntry> history(UUID accountId) {
        /* May seem unnecessary but makes sure account exists */
        Account account = get(accountId);

        return accountHistoryRepository.findByAccountId(account.id());
    }

    public Account deposit(UUID accountId, Money amount) {
        Account credited = transactions.inTransaction(() -> {
            Account account = get(accountId).credit(amount);

            accountRepository.update(account);
            accountHistoryRepository.insert(AccountHistoryEntry.deposit(accountId, amount));

            return account;
        });

        log.atInfo()
                .addKeyValue("accountId", accountId)
                .addKeyValue("amount", amount.amount().toPlainString())
                .addKeyValue("currency", amount.currency())
                .log("Deposit completed");

        return credited;
    }
}