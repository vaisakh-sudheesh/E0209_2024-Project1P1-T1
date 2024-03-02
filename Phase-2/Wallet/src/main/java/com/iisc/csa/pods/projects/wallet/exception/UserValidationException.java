package com.iisc.csa.pods.projects.wallet.exception;

public class UserValidationException extends RuntimeException {
    public UserValidationException(Integer user_id) {
        super("User verification failed for :"+user_id);
    }
}