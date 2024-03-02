/**
 * Repository for JPA Theatre Entity management
 */
package com.iisc.csa.pods.projects.bookingdatabase.repository;

import com.iisc.csa.pods.projects.bookingdatabase.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Integer> {
    <S extends Theatre> S save(S entity);

//    @Query("SELECT b from Theatre b WHERE b.id = :id")
//    Theatre findById (Integer id);
}
