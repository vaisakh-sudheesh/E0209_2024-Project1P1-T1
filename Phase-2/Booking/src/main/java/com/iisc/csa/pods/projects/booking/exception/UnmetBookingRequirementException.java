package com.iisc.csa.pods.projects.booking.exception;

public class UnmetBookingRequirementException extends RuntimeException {
    public UnmetBookingRequirementException(String errstr) {
        super("UnmetBookingRequirement :"+errstr);
    }
}