package com.thomascook.hotelbooking.repository;

import com.thomascook.hotelbooking.domain.Booking;
import com.thomascook.hotelbooking.domain.BookingStatus;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  /**
   * Counts bookings overlapping the half-open range [checkIn, checkOut). A booking ending exactly on
   * the requested check-in (same-day turnover) does not overlap.
   */
  @Query(
      "select count(b) from Booking b where b.hotel.id = :hotelId and b.status = :status"
          + " and b.checkIn < :checkOut and b.checkOut > :checkIn")
  long countOverlapping(
      @Param("hotelId") Long hotelId,
      @Param("status") BookingStatus status,
      @Param("checkIn") LocalDate checkIn,
      @Param("checkOut") LocalDate checkOut);

  Optional<Booking> findByReferenceIgnoreCase(String reference);

  List<Booking> findByUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);

  Optional<Booking> findByReferenceIgnoreCaseAndUserEmailIgnoreCase(String reference, String email);

  boolean existsByReferenceIgnoreCase(String reference);
}
