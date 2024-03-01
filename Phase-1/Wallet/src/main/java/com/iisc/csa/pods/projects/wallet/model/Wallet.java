/**
 * Model for Wallet JPA entity
 */
package com.iisc.csa.pods.projects.wallet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "wallet")
public class Wallet {
    /**
     * Primary key - ID field for Wallet entity.<br/><br/>
     */
    @Id
    @Column(name = "user_id")
    Integer user_id;

    /**
     * Balance amount for the associated wallet
     */
    @Column(name = "balance")
    Integer balance;

    // Constructors
    public Wallet(){}

    public Wallet(Integer user_id_, Integer balance_){
        this.balance = balance_;
        this.user_id = user_id_;
    }
}
