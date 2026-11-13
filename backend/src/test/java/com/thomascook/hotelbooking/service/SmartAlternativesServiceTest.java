package com.thomascook.hotelbooking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.thomascook.hotelbooking.domain.BookingStatus;
import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto;
import com.thomascook.hotelbooking.repository.BookingRepository;
import com.thomascook.hotelbooking.repository.HotelRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SmartAlternativesServiceTest {

  @Mock private HotelRepository hotelRepository;
  @Mock private BookingRepository bookingRepository;

  private SmartAlternativesService service() {
    // Built lazily: @Mock fields are only populated after test-class instantiation.
    AvailabilityService availabilityService = new AvailabilityService(bookingRepository);
    return new SmartAlternativesService(hotelRepository, availabilityService, new PricingService());
  }

  private Hotel hotel(Long id, String name, String location, String price, int rooms) {
    Hotel h = new Hotel(name, location, "desc", "WiFi", new BigDecimal(price), rooms);
    try {
      var idField = Hotel.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(h, id);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
    return h;
  }

  @Test
  void sold_out_hotel_returns_both_suggestions() {
    Hotel requested = hotel(1L, "Palolem Bay Retreat", "Goa", "4500.00", 1);
    Hotel cheaper = hotel(2L, "Baga Budget Inn", "Goa", "2200.00", 5);
    LocalDate in = LocalDate.parse("2026-12-20");
    LocalDate out = LocalDate.parse("2026-12-23");

    when(hotelRepository.findById(1L)).thenReturn(Optional.of(requested));
    when(hotelRepository.findByLocationIgnoreCaseOrderByPricePerNightAsc("Goa"))
        .thenReturn(List.of(cheaper, requested));
    // Same-hotel scan: +1 day still full, +2 days free.
    when(bookingRepository.countOverlapping(
            eq(1L), eq(BookingStatus.CONFIRMED), eq(in.plusDays(1)), eq(out.plusDays(1))))
        .thenReturn(1L);
    when(bookingRepository.countOverlapping(
            eq(1L), eq(BookingStatus.CONFIRMED), eq(in.plusDays(2)), eq(out.plusDays(2))))
        .thenReturn(0L);
    // Cheapest alternative has rooms for the original dates.
    when(bookingRepository.countOverlapping(
            eq(2L), eq(BookingStatus.CONFIRMED), eq(in), eq(out)))
        .thenReturn(0L);

    SmartAlternativesDto result = service().findAlternatives(1L, in, out);

    assertThat(result.nearestAvailable()).isNotNull();
    assertThat(result.nearestAvailable().checkIn()).isEqualTo(in.plusDays(2));
    assertThat(result.nearestAvailable().checkOut()).isEqualTo(out.plusDays(2));
    assertThat(result.nearestAvailable().nights()).isEqualTo(3);
    assertThat(result.cheapestAlternative()).isNotNull();
    assertThat(result.cheapestAlternative().hotelId()).isEqualTo(2L);
    assertThat(result.cheapestAlternative().available()).isTrue();
  }

  @Test
  void no_same_location_alternative_returns_null_cheapest() {
    Hotel requested = hotel(1L, "Solo Stay", "Leh", "3000.00", 1);
    LocalDate in = LocalDate.parse("2026-12-20");
    LocalDate out = LocalDate.parse("2026-12-21");

    when(hotelRepository.findById(1L)).thenReturn(Optional.of(requested));
    when(hotelRepository.findByLocationIgnoreCaseOrderByPricePerNightAsc("Leh"))
        .thenReturn(List.of(requested));
    when(bookingRepository.countOverlapping(
            eq(1L), any(BookingStatus.class), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(0L);

    SmartAlternativesDto result = service().findAlternatives(1L, in, out);

    assertThat(result.nearestAvailable()).isNotNull();
    assertThat(result.cheapestAlternative()).isNull();
  }
}
