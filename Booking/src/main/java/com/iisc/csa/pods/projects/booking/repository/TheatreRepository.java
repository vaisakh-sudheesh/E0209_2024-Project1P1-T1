package com.iisc.csa.pods.projects.booking.repository;

import com.iisc.csa.pods.projects.booking.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Integer> {
    <S extends Theatre> S save(S entity);

//    @Query("SELECT b from Theatre b WHERE b.id = :id")
//    Theatre findById (Integer id);
}
