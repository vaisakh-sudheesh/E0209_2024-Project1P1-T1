package com.iisc.csa.pods.projects.booking.exception;

public class WalletOperationException extends RuntimeException {
    public WalletOperationException(String operation) {
        super("Wallet "+operation+" operation failed.");
    }
}