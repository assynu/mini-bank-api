package com.bartczakdawid.minibank.domain;

import java.util.UUID;

public class SameAccountTransferException extends DomainException {
    public SameAccountTransferException(UUID account) {
        super("Illegal transfer to same account: " + account);
    }
}
