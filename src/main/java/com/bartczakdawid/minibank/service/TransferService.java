package com.bartczakdawid.minibank.service;

import com.bartczakdawid.minibank.domain.Account;
import com.bartczakdawid.minibank.domain.AccountHistoryEntry;
import com.bartczakdawid.minibank.domain.Money;
import com.bartczakdawid.minibank.domain.Transfer;
import com.bartczakdawid.minibank.repository.AccountHistoryRepository;
import com.bartczakdawid.minibank.repository.AccountRepository;
import com.bartczakdawid.minibank.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

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

    public Transfer transfer(String requestId, UUID fromAccountId, UUID toAccountId, Money amount) {
        Transfer transfer = Transfer.create(requestId, fromAccountId, toAccountId, amount);

        Optional<Transfer> existing = transferRepository.findByRequestId(requestId);
        if (existing.isPresent()) {
            return replay(existing.get(), fromAccountId, toAccountId, amount);
        }

        Transfer completed = transactions.inTransaction(() -> {
            boolean fromFirst = fromAccountId.compareTo(toAccountId) < 0;

            Account first = findAccount(fromFirst ? fromAccountId : toAccountId);
            Account second = findAccount(fromFirst ? toAccountId : fromAccountId);

            Account fromAccount = fromFirst ? first : second;
            Account toAccount = fromFirst ? second : first;

            Account updatedFromAccount = fromAccount.debit(amount);
            Account updatedToAccount = toAccount.credit(amount);

            accountRepository.update(updatedFromAccount);
            accountRepository.update(updatedToAccount);

            transferRepository.insert(transfer);

            accountHistoryRepository.insert(AccountHistoryEntry.transferDebit(transfer));
            accountHistoryRepository.insert(AccountHistoryEntry.transferCredit(transfer));

            return transfer;
        });

        log.atInfo()
                .addKeyValue("transferId", completed.id())
                .addKeyValue("clientRequestId", completed.requestId())
                .addKeyValue("fromAccountId", completed.fromAccountId())
                .addKeyValue("toAccountId", completed.toAccountId())
                .addKeyValue("amount", completed.amount().amount().toPlainString())
                .addKeyValue("currency", completed.amount().currency())
                .log("Transfer completed");

        return completed;
    }

    public Transfer get(UUID transferId) {
        return transferRepository.findById(transferId).orElseThrow(() -> new TransferNotFoundException(transferId));
    }

    private Transfer replay(Transfer existing, UUID fromAccountId, UUID toAccountId, Money amount) {
        if (!existing.sameRequestAs(fromAccountId, toAccountId, amount)) {
            throw new IdempotencyConflictException(existing.requestId());
        }

        log.atInfo()
                .addKeyValue("transferId", existing.id())
                .addKeyValue("clientRequestId", existing.requestId())
                .log("Transfer replayed");

        return existing;
    }

    private Account findAccount(UUID accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}