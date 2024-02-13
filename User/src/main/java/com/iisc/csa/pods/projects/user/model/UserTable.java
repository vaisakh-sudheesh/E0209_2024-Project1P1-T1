package com.iisc.csa.pods.projects.user.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class UserTable {
    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="useridgen")
        @SequenceGenerator(name="useridgen", initialValue = 1, allocationSize = 1)
    Integer id;

    @Column(name="name")
    private String name;

    @Column(name="email", unique=true)
    private String email;

    public UserTable(){}
    public UserTable(String name_, String email_ ) {
        this.name = name_;
        this.email = email_;
    }

    @Override
    public String toString() {
        return this.name+","+this.email;
    }
}
