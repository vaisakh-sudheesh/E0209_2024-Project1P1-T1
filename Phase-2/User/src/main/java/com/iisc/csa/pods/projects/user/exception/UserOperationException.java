package com.iisc.csa.pods.projects.user.exception;

public class UserOperationException extends RuntimeException {
    public UserOperationException(String operation) {
        super("User "+operation+"operation failed");
    }
}
