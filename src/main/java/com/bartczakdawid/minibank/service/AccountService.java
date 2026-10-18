package com.bartczakdawid.minibank.service;

import com.bartczakdawid.minibank.domain.Account;
import com.bartczakdawid.minibank.domain.AccountHistoryEntry;
import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.repository.AccountHistoryRepository;
import com.bartczakdawid.minibank.repository.AccountRepository;

import java.util.List;
import java.util.UUID;

public class AccountService {

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
        return transactions.inTransaction(() -> {
            Account credited = get(accountId).credit(amount);

            accountRepository.update(credited);
            accountHistoryRepository.insert(AccountHistoryEntry.deposit(accountId, amount));

            return credited;
        });
    }
}