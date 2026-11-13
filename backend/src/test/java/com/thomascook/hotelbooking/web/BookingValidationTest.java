package com.thomascook.hotelbooking.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thomascook.hotelbooking.exception.BookingNotFoundException;
import com.thomascook.hotelbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
class BookingValidationTest {

  @Autowired private MockMvc mvc;
  @MockBean private BookingService bookingService;

  private String bookingJson(String name, String email, String hotelId) {
    return """
        {"hotelId":%s,"guestName":"%s","email":"%s","checkIn":"2026-12-20","checkOut":"2026-12-22"}
        """
        .formatted(hotelId, name, email);
  }

  @Test
  void blank_guest_name_rejected() throws Exception {
    mvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson("", "asha@example.com", "1")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors[?(@.field=='guestName')].message").exists())
        .andExpect(jsonPath("$.path").value("/api/bookings"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void invalid_email_rejected() throws Exception {
    mvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson("Asha", "not-an-email", "1")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='email')].message").value("Enter a valid email address"));
  }

  @Test
  void missing_hotel_id_rejected() throws Exception {
    mvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson("Asha", "asha@example.com", "null")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field=='hotelId')]").exists());
  }

  @Test
  void malformed_date_rejected_with_format_hint() throws Exception {
    mvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"hotelId":1,"guestName":"Asha","email":"asha@example.com",\
                    "checkIn":"12-01-2026","checkOut":"2026-12-22"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Use YYYY-MM-DD for dates"));
  }

  @Test
  void missing_email_on_lookup_rejected() throws Exception {
    mvc.perform(get("/api/bookings/lookup").param("reference", "TC-AAAAAA"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Missing required parameter: email"));
  }

  @Test
  void wrong_email_lookup_returns_generic_404() throws Exception {
    when(bookingService.lookup(eq("TC-AAAAAA"), any())).thenThrow(new BookingNotFoundException());

    mvc.perform(
            get("/api/bookings/lookup").param("reference", "TC-AAAAAA").param("email", "x@y.com"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Booking not found for given reference and email"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"));
  }
}
