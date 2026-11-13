package com.thomascook.hotelbooking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.thomascook.hotelbooking.domain.BookingStatus;
import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

  @Mock private BookingRepository bookingRepository;

  @InjectMocks private AvailabilityService availabilityService;

  static Stream<Arguments> overlapMatrix() {
    LocalDate d10 = LocalDate.parse("2026-12-10");
    LocalDate d11 = LocalDate.parse("2026-12-11");
    LocalDate d12 = LocalDate.parse("2026-12-12");
    LocalDate d13 = LocalDate.parse("2026-12-13");
    LocalDate d09 = LocalDate.parse("2026-12-09");
    LocalDate d15 = LocalDate.parse("2026-12-15");
    return Stream.of(
        // existing [10,12) cases
        Arguments.of(d10, d12, d12, LocalDate.parse("2026-12-14"), false), // turnover: no overlap
        Arguments.of(d10, d12, d11, d13, true), // partial overlap
        Arguments.of(d10, d12, d09, d15, true), // new envelops existing
        Arguments.of(d09, d15, d10, d12, true), // existing envelops new
        Arguments.of(d10, d12, d10, d12, true), // identical range
        Arguments.of(d10, d12, LocalDate.parse("2026-12-08"), d10, false), // ends on check-in
        Arguments.of(d10, d12, LocalDate.parse("2026-12-08"), d09, false)); // fully before
  }

  @ParameterizedTest(name = "existing [{0},{1}) vs new [{2},{3}) -> overlap={4}")
  @MethodSource("overlapMatrix")
  void overlap_matrix(
      LocalDate existingIn, LocalDate existingOut, LocalDate newIn, LocalDate newOut,
      boolean expected) {
    assertThat(AvailabilityService.overlaps(existingIn, existingOut, newIn, newOut))
        .isEqualTo(expected);
    // Symmetry check: swapping sides must not change the answer.
    assertThat(AvailabilityService.overlaps(newIn, newOut, existingIn, existingOut))
        .isEqualTo(expected);
  }

  @Test
  void available_rooms_subtracts_confirmed_overlaps() {
    Hotel hotel = new Hotel("H", "Goa", "d", "WiFi", new BigDecimal("100.00"), 2);
    LocalDate in = LocalDate.parse("2026-12-20");
    LocalDate out = LocalDate.parse("2026-12-22");
    when(bookingRepository.countOverlapping(any(), eq(BookingStatus.CONFIRMED), eq(in), eq(out)))
        .thenReturn(1L);
    assertThat(availabilityService.availableRooms(hotel, in, out)).isEqualTo(1);
  }

  @Test
  void available_rooms_never_negative() {
    Hotel hotel = new Hotel("H", "Goa", "d", "WiFi", new BigDecimal("100.00"), 1);
    LocalDate in = LocalDate.parse("2026-12-20");
    LocalDate out = LocalDate.parse("2026-12-22");
    when(bookingRepository.countOverlapping(any(), eq(BookingStatus.CONFIRMED), eq(in), eq(out)))
        .thenReturn(5L);
    assertThat(availabilityService.availableRooms(hotel, in, out)).isZero();
  }
}
