package com.bartczakdawid.minibank.repository;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.ThreadLocalTransactionProvider;

import javax.sql.DataSource;

public final class Database {

    private Database() {
    }

    public static DSLContext createDslContext(DataSource dataSource) {
        var configuration = new DefaultConfiguration().set(SQLDialect.POSTGRES).set(new ThreadLocalTransactionProvider(new DataSourceConnectionProvider(dataSource)));

        return DSL.using(configuration);
    }
}