package com.thomascook.hotelbooking.repository;

import com.thomascook.hotelbooking.domain.Hotel;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

  Optional<Hotel> findByNameAndLocation(String name, String location);

  Optional<Hotel> findBySourceAndSourceId(String source, String sourceId);

  List<Hotel> findByLocationContainingIgnoreCaseOrderByPricePerNightAsc(String location);

  List<Hotel> findByLocationIgnoreCaseOrderByPricePerNightAsc(String location);

  List<Hotel> findAllByOrderByPricePerNightAsc();

  List<Hotel> findBySourceOrderByPricePerNightAsc(String source);

  List<Hotel> findBySourceAndLocationContainingIgnoreCaseOrderByPricePerNightAsc(String source, String location);

  @Modifying
  @Query("delete from Hotel h where (h.source is null or h.source <> :source) and not exists (select b.id from Booking b where b.hotel.id = h.id)")
  int deleteUnbookedOutsideSource(@Param("source") String source);

  /** Pessimistic lock used by booking creation to serialize the overlap check + insert. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select h from Hotel h where h.id = :id")
  Optional<Hotel> findByIdForUpdate(@Param("id") Long id);
}
