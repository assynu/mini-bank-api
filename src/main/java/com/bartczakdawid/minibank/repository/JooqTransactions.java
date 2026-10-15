package com.bartczakdawid.minibank.repository;

import com.bartczakdawid.minibank.service.Transactions;
import org.jooq.DSLContext;

import java.util.function.Supplier;

public class JooqTransactions implements Transactions {

    private final DSLContext dsl;

    public JooqTransactions(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return dsl.transactionResult(work::get);
    }
}