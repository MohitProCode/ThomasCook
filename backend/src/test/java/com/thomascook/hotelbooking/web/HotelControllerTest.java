package com.thomascook.hotelbooking.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thomascook.hotelbooking.dto.AvailabilityDto;
import com.thomascook.hotelbooking.dto.HotelDetailDto;
import com.thomascook.hotelbooking.dto.HotelSummaryDto;
import com.thomascook.hotelbooking.dto.PageResponse;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto.CheapestAlternativeDto;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto.NearestAvailableDto;
import com.thomascook.hotelbooking.exception.HotelNotFoundException;
import com.thomascook.hotelbooking.exception.InvalidDateRangeException;
import com.thomascook.hotelbooking.service.HotelService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

  @Autowired private MockMvc mvc;
  @MockBean private HotelService hotelService;

  @Test
  void search_returns_paged_content() throws Exception {
    HotelSummaryDto summary =
        new HotelSummaryDto(
            1L, "Palolem Bay Retreat", "Goa", "desc", List.of("WiFi"),
            new BigDecimal("4500.00"), 20, 14, 2, new BigDecimal("9000.00"));
    when(hotelService.search(eq("Goa"), any(), any(), eq(0), eq(10)))
        .thenReturn(new PageResponse<>(List.of(summary), 0, 10, 1));

    mvc.perform(
            get("/api/hotels")
                .param("location", "Goa")
                .param("checkIn", "2026-12-20")
                .param("checkOut", "2026-12-22"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Palolem Bay Retreat"))
        .andExpect(jsonPath("$.content[0].availableRooms").value(14))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void detail_sold_out_includes_smart_alternatives() throws Exception {
    LocalDate in = LocalDate.parse("2026-12-20");
    LocalDate out = LocalDate.parse("2026-12-22");
    SmartAlternativesDto alternatives =
        new SmartAlternativesDto(
            new NearestAvailableDto(
                LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-27"), 2, 4,
                "Earliest dates with rooms for the same stay length"),
            new CheapestAlternativeDto(
                2L, "Baga Budget Inn", "Goa", new BigDecimal("2200.00"), 7, true, in, out,
                "Cheapest hotel in Goa with rooms for your dates"));
    HotelDetailDto detail =
        new HotelDetailDto(
            1L, "Palolem Bay Retreat", "Goa", "desc", List.of("WiFi"),
            new BigDecimal("4500.00"), 20, 0, new AvailabilityDto(in, out, 2, 0), alternatives);
    when(hotelService.getDetail(eq(1L), any(), any())).thenReturn(detail);

    mvc.perform(get("/api/hotels/1").param("checkIn", "2026-12-20").param("checkOut", "2026-12-22"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.availableRooms").value(0))
        .andExpect(jsonPath("$.smartAlternatives.nearestAvailable.checkIn").value("2026-12-25"))
        .andExpect(
            jsonPath("$.smartAlternatives.cheapestAlternative.hotelId").value(2));
  }

  @Test
  void unknown_hotel_returns_404_envelope() throws Exception {
    when(hotelService.getDetail(eq(999L), any(), any()))
        .thenThrow(new HotelNotFoundException(999L));

    mvc.perform(get("/api/hotels/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Hotel not found: 999"))
        .andExpect(jsonPath("$.path").value("/api/hotels/999"));
  }

  @Test
  void bad_date_range_returns_400_envelope() throws Exception {
    when(hotelService.getDetail(eq(1L), any(), any()))
        .thenThrow(new InvalidDateRangeException("Check-out date must be after check-in date"));

    mvc.perform(get("/api/hotels/1").param("checkIn", "2026-12-22").param("checkOut", "2026-12-20"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Check-out date must be after check-in date"));
  }

  @Test
  void malformed_query_date_returns_format_hint() throws Exception {
    mvc.perform(
            get("/api/hotels").param("checkIn", "12-01-2026").param("checkOut", "2026-12-22"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Use YYYY-MM-DD for dates"));
  }
}
