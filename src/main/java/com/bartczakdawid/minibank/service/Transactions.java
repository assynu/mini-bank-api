package com.bartczakdawid.minibank.service;

import java.util.function.Supplier;

public interface Transactions {
    <T> T inTransaction(Supplier<T> work);
}