package com.thomascook.hotelbooking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thomascook.hotelbooking.domain.BookingStatus;
import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.dto.BookingResponse;
import com.thomascook.hotelbooking.dto.CreateBookingRequest;
import com.thomascook.hotelbooking.exception.BookingAlreadyCancelledException;
import com.thomascook.hotelbooking.exception.BookingNotFoundException;
import com.thomascook.hotelbooking.exception.InvalidDateRangeException;
import com.thomascook.hotelbooking.exception.SoldOutException;
import com.thomascook.hotelbooking.repository.BookingRepository;
import com.thomascook.hotelbooking.repository.HotelRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceTest {

  @Autowired private BookingService bookingService;
  @Autowired private HotelRepository hotelRepository;
  @Autowired private BookingRepository bookingRepository;

  private Hotel oneRoomHotel;
  private Hotel goaAlternative;

  @BeforeEach
  void seed() {
    bookingRepository.deleteAll();
    hotelRepository.deleteAll();
    oneRoomHotel =
        hotelRepository.save(
            new Hotel(
                "Single Room House",
                "Goa",
                "One-room test hotel",
                "WiFi",
                new BigDecimal("1000.00"),
                1));
    goaAlternative =
        hotelRepository.save(
            new Hotel(
                "Goa Spare Rooms", "Goa", "Spare capacity", "WiFi,Pool",
                new BigDecimal("800.00"), 5));
  }

  private CreateBookingRequest request(Hotel hotel, LocalDate in, LocalDate out) {
    CreateBookingRequest req = new CreateBookingRequest();
    req.setHotelId(hotel.getId());
    req.setGuestName("Asha Verma");
    req.setEmail("Asha@Example.COM");
    req.setCheckIn(in);
    req.setCheckOut(out);
    return req;
  }

  @Test
  void create_computes_total_server_side() {
    LocalDate in = LocalDate.now().plusDays(10);
    LocalDate out = in.plusDays(3);

    BookingResponse created = bookingService.create(request(oneRoomHotel, in, out));

    assertThat(created.status()).isEqualTo(BookingStatus.CONFIRMED);
    assertThat(created.nights()).isEqualTo(3);
    assertThat(created.totalPrice()).isEqualByComparingTo(new BigDecimal("3000.00"));
    assertThat(created.totalAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));
    assertThat(created.email()).isEqualTo("asha@example.com");
    assertThat(created.reference()).matches("^TC-[ABCDEFGHJKMNPQRSTUVWXYZ23456789]{6}$");
  }

  @Test
  void second_overlapping_booking_rejected_with_alternatives() {
    LocalDate in = LocalDate.now().plusDays(10);
    LocalDate out = in.plusDays(2);
    bookingService.create(request(oneRoomHotel, in, out));

    assertThatThrownBy(() -> bookingService.create(request(oneRoomHotel, in, out)))
        .isInstanceOf(SoldOutException.class)
        .hasMessageContaining("sold out");
    try {
      bookingService.create(request(oneRoomHotel, in.plusDays(30), out.plusDays(30)));
    } catch (SoldOutException e) {
      throw new AssertionError("Non-overlapping dates should be bookable", e);
    }
  }

  @Test
  void sold_out_exception_carries_smart_alternatives() {
    LocalDate in = LocalDate.now().plusDays(10);
    LocalDate out = in.plusDays(2);
    bookingService.create(request(oneRoomHotel, in, out));

    try {
      bookingService.create(request(oneRoomHotel, in, out));
      throw new AssertionError("Expected SoldOutException");
    } catch (SoldOutException e) {
      assertThat(e.getSmartAlternatives()).isNotNull();
      assertThat(e.getSmartAlternatives().nearestAvailable()).isNotNull();
      assertThat(e.getSmartAlternatives().nearestAvailable().checkIn()).isEqualTo(out);
      assertThat(e.getSmartAlternatives().cheapestAlternative()).isNotNull();
      assertThat(e.getSmartAlternatives().cheapestAlternative().hotelId())
          .isEqualTo(goaAlternative.getId());
    }
  }

  @Test
  void same_day_turnover_does_not_overlap() {
    LocalDate in = LocalDate.now().plusDays(10);
    LocalDate mid = in.plusDays(2);
    LocalDate out = mid.plusDays(2);
    bookingService.create(request(oneRoomHotel, in, mid));

    BookingResponse second = bookingService.create(request(oneRoomHotel, mid, out));
    assertThat(second.status()).isEqualTo(BookingStatus.CONFIRMED);
  }

  @Test
  void cancel_releases_slot_for_rebook() {
    LocalDate in = LocalDate.now().plusDays(10);
    LocalDate out = in.plusDays(2);
    BookingResponse created = bookingService.create(request(oneRoomHotel, in, out));

    bookingService.cancel(created.reference(), "asha@example.com");

    BookingResponse rebooked = bookingService.create(request(oneRoomHotel, in, out));
    assertThat(rebooked.status()).isEqualTo(BookingStatus.CONFIRMED);
    assertThat(rebooked.reference()).isNotEqualTo(created.reference());
    assertThat(bookingService.lookup(created.reference(), "asha@example.com").status())
        .isEqualTo(BookingStatus.CANCELLED);
  }

  @Test
  void double_cancel_is_conflict() {
    LocalDate in = LocalDate.now().plusDays(10);
    BookingResponse created =
        bookingService.create(request(oneRoomHotel, in, in.plusDays(1)));
    bookingService.cancel(created.reference(), "asha@example.com");

    assertThatThrownBy(() -> bookingService.cancel(created.reference(), "asha@example.com"))
        .isInstanceOf(BookingAlreadyCancelledException.class);
  }

  @Test
  void wrong_email_returns_generic_not_found() {
    LocalDate in = LocalDate.now().plusDays(10);
    BookingResponse created =
        bookingService.create(request(oneRoomHotel, in, in.plusDays(1)));

    assertThatThrownBy(() -> bookingService.lookup(created.reference(), "wrong@example.com"))
        .isInstanceOf(BookingNotFoundException.class)
        .hasMessage("Booking not found for given reference and email");
  }

  @Test
  void past_checkin_rejected() {
    assertThatThrownBy(
            () ->
                bookingService.create(
                    request(oneRoomHotel, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))))
        .isInstanceOf(InvalidDateRangeException.class)
        .hasMessage("Check-in date cannot be in the past");
  }

  @Test
  void checkout_on_or_before_checkin_rejected() {
    LocalDate in = LocalDate.now().plusDays(5);
    assertThatThrownBy(() -> bookingService.create(request(oneRoomHotel, in, in)))
        .isInstanceOf(InvalidDateRangeException.class)
        .hasMessage("Check-out date must be after check-in date");
    assertThatThrownBy(() -> bookingService.create(request(oneRoomHotel, in, in.minusDays(1))))
        .isInstanceOf(InvalidDateRangeException.class)
        .hasMessage("Check-out date must be after check-in date");
  }

  @Test
  void booking_uses_current_price_not_stale_quote() {
    LocalDate in = LocalDate.now().plusDays(10);
    LocalDate out = in.plusDays(2);
    oneRoomHotel.setPricePerNight(new BigDecimal("2500.00"));
    hotelRepository.saveAndFlush(oneRoomHotel);

    BookingResponse created = bookingService.create(request(oneRoomHotel, in, out));
    assertThat(created.pricePerNight()).isEqualByComparingTo(new BigDecimal("2500.00"));
    assertThat(created.totalPrice()).isEqualByComparingTo(new BigDecimal("5000.00"));
  }

  @Test
  void references_are_unique() {
    Set<String> references = new HashSet<>();
    for (int i = 0; i < 25; i++) {
      LocalDate in = LocalDate.now().plusDays(40 + i * 3);
      BookingResponse created =
          bookingService.create(request(goaAlternative, in, in.plusDays(1)));
      assertThat(references.add(created.reference())).isTrue();
    }
  }
}
