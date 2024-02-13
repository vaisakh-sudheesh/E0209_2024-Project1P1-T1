package com.iisc.csa.pods.projects.user.repository;
import com.iisc.csa.pods.projects.user.model.UserTable;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRepository extends JpaRepository <UserTable,Integer> {
    <S extends UserTable> S save(S entity);

    @Query("SELECT b FROM UserTable b WHERE b.id = :id")
    UserTable findbyId(Integer id);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserTable  b WHERE b.id = :id")
    void deletebyId(Integer id);

    boolean existsById(Integer id);
}
