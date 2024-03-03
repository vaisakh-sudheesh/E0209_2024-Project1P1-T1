/**
 * Interface for JPA repository for Wallet module
 *
 */
package com.iisc.csa.pods.projects.wallet.repository;

import com.iisc.csa.pods.projects.wallet.model.Wallet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceContext;

@Repository
public class WalletRepository {
    @PersistenceContext
    @Autowired
    private EntityManager entityManager;

    @Transactional
    public <S extends Wallet> S save (S entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    public Wallet findByUser_id(Integer id){
        return entityManager.find(Wallet.class, id);
    }

    @Transactional
    @Modifying(clearAutomatically=true, flushAutomatically=true)
    public void deleteByUser_id(Integer id) {
        Wallet wallet = findByUser_id(id);
        if (wallet != null){
            entityManager.remove(wallet);
        }
    }

    @Transactional
    @Modifying(clearAutomatically=true, flushAutomatically=true)
    public void deleteAll() {
        Query query = entityManager.createQuery("DELETE FROM Wallet");
        query.executeUpdate();
    }

    public boolean existsByUser_id(Integer id) {
        return findByUser_id(id) != null;
    }
}

