/**
 * Interface for JPA repository for Wallet module
 *
 */
package com.iisc.csa.pods.projects.wallet.repository;

import com.iisc.csa.pods.projects.wallet.model.Wallet;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    @Override
    <S extends Wallet> S save (S entity);

    @Query("SELECT b FROM Wallet b WHERE b.user_id = :id")
    Wallet findByUser_id(Integer id);

    @Transactional
    @Modifying
    @Query("DELETE FROM Wallet b WHERE b.user_id = :id")
    void deleteByUser_id(Integer id);

    @Query("SELECT CASE WHEN count(b) > 0 THEN true ELSE false END FROM Wallet b where b.user_id = ?1")
    boolean existsByUser_id(Integer id);
}
