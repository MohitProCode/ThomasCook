package com.thomascook.hotelbooking.web;

import com.thomascook.hotelbooking.dto.BookingResponse;
import com.thomascook.hotelbooking.dto.CancelBookingRequest;
import com.thomascook.hotelbooking.dto.CancelBookingResponse;
import com.thomascook.hotelbooking.dto.CreateBookingRequest;
import com.thomascook.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
    return bookingService.create(request);
  }

  @GetMapping("/mine")
  public List<BookingResponse> mine() { return bookingService.mine(); }

  /** Primary lookup: GET /api/bookings/lookup?reference=TC-XXXXXX&email=a@b.com */
  @GetMapping("/lookup")
  public BookingResponse lookup(
      @RequestParam String reference, @RequestParam String email) {
    return bookingService.lookup(reference, email);
  }

  /** Alias lookup: GET /api/bookings/{reference}?email=a@b.com */
  @GetMapping("/{reference}")
  public BookingResponse retrieve(
      @PathVariable String reference, @RequestParam(required = false) String email) {
    return bookingService.lookup(reference, email);
  }

  /**
   * Cancel: POST /api/bookings/{reference}/cancel with the email either as a query param
   * ({@code ?email=}) or as a JSON body ({@code {"email": "..."}}).
   */
  @PostMapping("/{reference}/cancel")
  public CancelBookingResponse cancel(
      @PathVariable String reference,
      @RequestParam(required = false) String email,
      @RequestBody(required = false) CancelBookingRequest body) {
    String resolved = email;
    if ((resolved == null || resolved.isBlank()) && body != null) {
      resolved = body.getEmail();
    }
    return bookingService.cancel(reference, resolved);
  }
}
