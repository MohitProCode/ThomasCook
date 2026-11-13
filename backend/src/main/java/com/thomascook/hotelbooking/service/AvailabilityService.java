package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.domain.BookingStatus;
import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.repository.BookingRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

  private final BookingRepository bookingRepository;

  public AvailabilityService(BookingRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
  }

  /**
   * Half-open overlap test: [aStart, aEnd) overlaps [bStart, bEnd) iff aStart < bEnd and aEnd >
   * bStart. Same-day turnover (aEnd == bStart) does not overlap.
   */
  public static boolean overlaps(
      LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
    return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
  }

  /** Free rooms = total rooms minus CONFIRMED bookings overlapping the requested range. */
  public int availableRooms(Hotel hotel, LocalDate checkIn, LocalDate checkOut) {
    long taken =
        bookingRepository.countOverlapping(hotel.getId(), BookingStatus.CONFIRMED, checkIn, checkOut);
    return (int) Math.max(0, hotel.getTotalRooms() - taken);
  }

  public int availableRooms(Long hotelId, int totalRooms, LocalDate checkIn, LocalDate checkOut) {
    long taken =
        bookingRepository.countOverlapping(hotelId, BookingStatus.CONFIRMED, checkIn, checkOut);
    return (int) Math.max(0, totalRooms - taken);
  }
}
