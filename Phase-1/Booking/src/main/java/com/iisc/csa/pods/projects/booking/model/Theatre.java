/**
 * Model for Theatre JPA entity
 */
package com.iisc.csa.pods.projects.booking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *  JPA Model class for Theatre.
 */
@Entity
@Data
@Table(name="Theatre")
@Setter
@Getter
public class Theatre {
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
     * Name of theatre<br/><br/>
     *
     * No validations steps are added as input will be read from Theatres.csv by CommandLineRunner.
     */
    @Column (name = "name")
    String name;

    /**
     * Location of the theatre<br/><br/>
     *
     * No validations steps are added as input will be read from Theatres.csv by CommandLineRunner.
     */
    @Column (name = "location")
    String location;

    public Theatre(){}

    public Theatre(Integer id_, String name_, String location_){
        this.id = id_;
        this.name = name_;
        this.location = location_;
    }
}
