/**
 * Model for Show JPA entity
 */
package com.iisc.csa.pods.projects.booking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Table(name = "Show")
@Getter
@Setter
public class Show {
    @Id
    @Column(name = "id")
    Integer id;

    @Column(name = "theatre_id")
    Integer theatre_id;

    @Column(name = "title")
    String title;

    @Column(name = "price")
    Integer price;

    @Column(name = "seats_available")
    Integer seats_available;

    public Show(){}

    public Show(Integer id_, Integer theatre_id_, String title_, Integer price_, Integer seats_available_){
        this.id = id_;
        this.theatre_id = theatre_id_;
        this.title = title_;
        this.price = price_;
        this.seats_available = seats_available_;
    }
}
