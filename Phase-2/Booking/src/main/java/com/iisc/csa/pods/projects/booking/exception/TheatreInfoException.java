package com.iisc.csa.pods.projects.booking.exception;

public class TheatreInfoException extends RuntimeException {
    public TheatreInfoException(Integer theater_id) {
        super("Incorrect theatre information:"+theater_id);
    }
}
