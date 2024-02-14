/**
 * Model for Booking request payload information submitted from client as part of POST request requirement.
 */
package com.iisc.csa.pods.projects.booking.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingPayload {
    Integer show_id;
    Integer user_id;
    Integer seats_booked;
}
