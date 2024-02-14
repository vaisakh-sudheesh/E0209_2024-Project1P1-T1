/**
 * Model for Booking JPA entity.
 */
package com.iisc.csa.pods.projects.booking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Table(name="Booking")
@Setter
@Getter
public class Booking {
    @Id
            @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="bookingidgen")
            @SequenceGenerator(name = "bookingidgen", initialValue = 1, allocationSize = 1)
    Integer id;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn(name = "show_id")
    Show  show_id;

    @Column (name = "user_id")
    Integer user_id;

    @Column (name = "seats_booked")
    Integer seats_booked;

    public Booking(){}
    public Booking(Show show_id_, Integer user_id_, Integer seats_booked_) {
        this.show_id = show_id_;
        this.user_id = user_id_;
        this.seats_booked = seats_booked_;
    }

}
