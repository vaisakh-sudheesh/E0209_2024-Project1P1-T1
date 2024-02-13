package com.iisc.csa.pods.projects.booking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Entity
@Data
@Table(name="Theatre")
@Setter
@Getter
public class Theatre {
    @Id
    @Column(name = "id")
    Integer id;

    @Column (name = "name")
    String name;

    @Column (name = "location")
    String location;

    public Theatre(){}

    public Theatre(Integer id_, String name_, String location_){
        this.id = id_;
        this.name = name_;
        this.location = location_;
    }
}
