package com.thomascook.hotelbooking.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thomascook.hotelbooking.domain.Booking;
import com.thomascook.hotelbooking.domain.BookingStatus;
import com.thomascook.hotelbooking.domain.Hotel;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

// Default @DataJpaTest embedded H2 (unique per context) with MySQL-dialect data.sql disabled.
@DataJpaTest
@TestPropertySource(
    properties = {
      "spring.sql.init.mode=never",
      "spring.jpa.hibernate.ddl-auto=create-drop"
    })
class BookingRepositoryTest {

  @Autowired private BookingRepository bookingRepository;
  @Autowired private HotelRepository hotelRepository;
  @Autowired private TestEntityManager entities;

  private static final java.util.concurrent.atomic.AtomicInteger HOTEL_SEQ =
      new java.util.concurrent.atomic.AtomicInteger();

  private Hotel persistHotel() {
    return hotelRepository.save(
        new Hotel(
            "Repo Hotel " + HOTEL_SEQ.incrementAndGet(),
            "Goa",
            "desc",
            "WiFi",
            new BigDecimal("1000.00"),
            2));
  }

  private Booking persistBooking(
      Hotel hotel, String ref, LocalDate in, LocalDate out, BookingStatus status) {
    Booking booking =
        new Booking(
            ref,
            hotel,
            "Guest",
            "guest@example.com",
            in,
            out,
            2,
            new BigDecimal("1000.00"),
            new BigDecimal("2000.00"));
    booking.setStatus(status);
    return bookingRepository.saveAndFlush(booking);
  }

  @Test
  void overlap_count_includes_partial_and_enclosing_ranges() {
    Hotel hotel = persistHotel();
    persistBooking(
        hotel,
        "TC-AAAAAA",
        LocalDate.parse("2026-12-10"),
        LocalDate.parse("2026-12-12"),
        BookingStatus.CONFIRMED);

    // Partial overlap.
    assertThat(
            bookingRepository.countOverlapping(
                hotel.getId(),
                BookingStatus.CONFIRMED,
                LocalDate.parse("2026-12-11"),
                LocalDate.parse("2026-12-13")))
        .isEqualTo(1);
    // New range envelops the existing booking.
    assertThat(
            bookingRepository.countOverlapping(
                hotel.getId(),
                BookingStatus.CONFIRMED,
                LocalDate.parse("2026-12-09"),
                LocalDate.parse("2026-12-15")))
        .isEqualTo(1);
    // New range enclosed by the existing booking.
    Hotel hotel2 = persistHotel();
    persistBooking(
        hotel2,
        "TC-BBBBBB",
        LocalDate.parse("2026-12-09"),
        LocalDate.parse("2026-12-15"),
        BookingStatus.CONFIRMED);
    assertThat(
            bookingRepository.countOverlapping(
                hotel2.getId(),
                BookingStatus.CONFIRMED,
                LocalDate.parse("2026-12-10"),
                LocalDate.parse("2026-12-12")))
        .isEqualTo(1);
  }

  @Test
  void same_day_turnover_is_not_counted() {
    Hotel hotel = persistHotel();
    persistBooking(
        hotel,
        "TC-CCCCCC",
        LocalDate.parse("2026-12-10"),
        LocalDate.parse("2026-12-12"),
        BookingStatus.CONFIRMED);

    assertThat(
            bookingRepository.countOverlapping(
                hotel.getId(),
                BookingStatus.CONFIRMED,
                LocalDate.parse("2026-12-12"),
                LocalDate.parse("2026-12-14")))
        .isZero();
  }

  @Test
  void cancelled_bookings_are_ignored() {
    Hotel hotel = persistHotel();
    persistBooking(
        hotel,
        "TC-DDDDDD",
        LocalDate.parse("2026-12-10"),
        LocalDate.parse("2026-12-12"),
        BookingStatus.CANCELLED);

    assertThat(
            bookingRepository.countOverlapping(
                hotel.getId(),
                BookingStatus.CONFIRMED,
                LocalDate.parse("2026-12-10"),
                LocalDate.parse("2026-12-12")))
        .isZero();
  }

  @Test
  void duplicate_reference_violates_unique_constraint() {
    Hotel hotel = persistHotel();
    persistBooking(
        hotel,
        "TC-EEEEEE",
        LocalDate.parse("2026-12-10"),
        LocalDate.parse("2026-12-12"),
        BookingStatus.CONFIRMED);
    entities.clear();

    Booking duplicate =
        new Booking(
            "TC-EEEEEE",
            hotelRepository.findById(hotel.getId()).orElseThrow(),
            "Other",
            "other@example.com",
            LocalDate.parse("2026-12-20"),
            LocalDate.parse("2026-12-21"),
            1,
            new BigDecimal("1000.00"),
            new BigDecimal("1000.00"));
    assertThatThrownBy(() -> bookingRepository.saveAndFlush(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
