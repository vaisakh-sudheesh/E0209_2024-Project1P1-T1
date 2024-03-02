package com.iisc.csa.pods.projects.wallet.exception;

public class WalletOperationException extends RuntimeException {
    public WalletOperationException(String operation, String reason) {
        super("Wallet "+operation+" operation failed, reason = "+reason);
    }
}