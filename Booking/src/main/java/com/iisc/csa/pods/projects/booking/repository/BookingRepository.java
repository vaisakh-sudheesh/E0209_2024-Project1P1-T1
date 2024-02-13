package com.iisc.csa.pods.projects.booking.repository;

import com.iisc.csa.pods.projects.booking.model.Booking;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    <S extends Booking> S save (S entity);

    @Query("SELECT b FROM Booking b WHERE b.user_id = :user_id")
    List<Booking> findByUser_id(Integer user_id);

    @Query("SELECT b from Booking b WHERE b.user_id = :user_id AND b.show_id = :show_id")
    List<Booking> findAllByUser_idAndShow_id(Integer user_id, Integer show_id);

    @Query("SELECT CASE WHEN count(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.user_id = :user_id AND b.show_id = :show_id")
    boolean existsByUser_idAndShow_id(Integer user_id, Integer show_id);

@Transactional
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.user_id = :user_id")
    void deleteByUser_id(Integer user_id);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.user_id = :user_id AND b.show_id = :show_id")
    void deleteAllByUser_idAndShow_id (Integer user_id, Integer show_id);

}
