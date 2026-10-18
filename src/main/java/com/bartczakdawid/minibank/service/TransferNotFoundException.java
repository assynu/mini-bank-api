package com.bartczakdawid.minibank.service;

import java.util.UUID;

public class TransferNotFoundException extends RuntimeException {
    public TransferNotFoundException(UUID transferId) {
        super("Transfer with id " + transferId + " not found");
    }
}
