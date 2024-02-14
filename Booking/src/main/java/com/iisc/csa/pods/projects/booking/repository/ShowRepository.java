/**
 * Repository for JPA Shows Entity management
 */
package com.iisc.csa.pods.projects.booking.repository;

import com.iisc.csa.pods.projects.booking.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Integer> {
    <S extends Show> S save (S entity);

    @Query("SELECT b FROM Show b WHERE b.theatre_id = :theater_id")
    List<Show> findByTheatre_id(Integer theater_id);

    @Query("SELECT b from Show b WHERE b.id = :show_id")
    Show findByShowId(Integer show_id);
}
