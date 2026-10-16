package com.bartczakdawid.minibank.service;

import com.bartczakdawid.minibank.domain.Account;
import com.bartczakdawid.minibank.domain.AccountHistoryEntry;
import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.domain.Transfer;
import com.bartczakdawid.minibank.repository.AccountHistoryRepository;
import com.bartczakdawid.minibank.repository.AccountRepository;
import com.bartczakdawid.minibank.repository.TransferRepository;

import java.util.UUID;

public class TransferService {
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final TransferRepository transferRepository;
    private final Transactions transactions;

    public TransferService(AccountRepository accountRepository, AccountHistoryRepository accountHistoryRepository, TransferRepository transferRepository, Transactions transactions) {
        this.accountRepository = accountRepository;
        this.accountHistoryRepository = accountHistoryRepository;
        this.transferRepository = transferRepository;
        this.transactions = transactions;
    }

    public Transfer transfer(UUID fromAccountId, UUID toAccountId, Money amount) {
        Transfer transfer = Transfer.create(fromAccountId, toAccountId, amount);

        return transactions.inTransaction(() -> {
            Account fromAccount = findAccount(fromAccountId);
            Account toAccount = findAccount(toAccountId);

            Account updatedFromAccount = fromAccount.debit(amount);
            Account updatedToAccount = toAccount.credit(amount);

            accountRepository.update(updatedFromAccount);
            accountRepository.update(updatedToAccount);

            transferRepository.insert(transfer);

            accountHistoryRepository.insert(AccountHistoryEntry.transferDebit(transfer));
            accountHistoryRepository.insert(AccountHistoryEntry.transferCredit(transfer));

            return transfer;
        });
    }

    private Account findAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
