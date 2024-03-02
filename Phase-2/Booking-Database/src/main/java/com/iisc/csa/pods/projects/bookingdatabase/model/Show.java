/**
 * Model for Show JPA entity
 */
package com.iisc.csa.pods.projects.bookingdatabase.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA Model class for Show.
 */
@Entity
@Data
@Table(name = "Show")
@Getter
@Setter
public class Show {
    /**
     * Primary key - ID field for unique identifier of Show.<br/><br/>
     *
     * Since the values are initialized from 'Shows.csv' by runner,
     * a generator need not be associated with this field.<br/>
     */
    @Id
    @Column(name = "id")
    Integer id;

    /**
     * Theatre ID for show
     */
    @Column(name = "theatre_id")
    Integer theatre_id;

    /**
     * Title of show
     */
    @Column(name = "title")
    String title;

    /**
     * Price of ticket
     */
    @Column(name = "price")
    Integer price;

    /**
     * Number of seats available
     */
    @Column(name = "seats_available")
    Integer seats_available;

    // Constructors

    public Show(){}

    public Show(Integer id_, Integer theatre_id_, String title_, Integer price_, Integer seats_available_){
        this.id = id_;
        this.theatre_id = theatre_id_;
        this.title = title_;
        this.price = price_;
        this.seats_available = seats_available_;
    }
}
