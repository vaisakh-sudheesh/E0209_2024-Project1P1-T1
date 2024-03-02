package com.iisc.csa.pods.projects.booking.exception;

public class ShowInfoException extends RuntimeException {
    public ShowInfoException(Integer show_id) {
        super("Incorrect show information:"+show_id);
    }
}
