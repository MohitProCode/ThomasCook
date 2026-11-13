package com.thomascook.hotelbooking.web;

import com.thomascook.hotelbooking.dto.HotelDetailDto;
import com.thomascook.hotelbooking.dto.AvailabilityCalendarDto;
import com.thomascook.hotelbooking.dto.HotelSummaryDto;
import com.thomascook.hotelbooking.dto.PageResponse;
import com.thomascook.hotelbooking.dto.QuoteRequest;
import com.thomascook.hotelbooking.dto.QuoteResponse;
import com.thomascook.hotelbooking.service.HotelService;
import com.thomascook.hotelbooking.service.NuiteeSearchService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

  private final HotelService hotelService;
  private final NuiteeSearchService nuiteeSearchService;

  public HotelController(HotelService hotelService, NuiteeSearchService nuiteeSearchService) {
    this.hotelService = hotelService;
    this.nuiteeSearchService = nuiteeSearchService;
  }

  @GetMapping
  public PageResponse<HotelSummaryDto> search(
      @RequestParam String location,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
      @RequestParam(defaultValue = "2") int guests,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return nuiteeSearchService.search(location, checkIn, checkOut, guests, page, size);
  }

  @GetMapping("/{id}")
  public HotelDetailDto detail(
      @PathVariable Long id,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
    return hotelService.getDetail(id, checkIn, checkOut);
  }

  @PostMapping("/{id}/quote")
  public QuoteResponse quote(@PathVariable Long id, @Valid @RequestBody QuoteRequest request) {
    return hotelService.quote(id, request.getCheckIn(), request.getCheckOut());
  }

  @GetMapping("/{id}/availability")
  public AvailabilityCalendarDto availability(
      @PathVariable Long id,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return hotelService.availabilityCalendar(id, from, to);
  }
}
