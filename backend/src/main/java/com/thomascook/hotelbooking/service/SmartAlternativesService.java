package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto.CheapestAlternativeDto;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto.NearestAvailableDto;
import com.thomascook.hotelbooking.exception.HotelNotFoundException;
import com.thomascook.hotelbooking.repository.HotelRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmartAlternativesService {

  /** How far forward to look for the next available window for the same stay length. */
  static final int MAX_SHIFT_DAYS = 30;

  private final HotelRepository hotelRepository;
  private final AvailabilityService availabilityService;
  private final PricingService pricingService;

  public SmartAlternativesService(
      HotelRepository hotelRepository,
      AvailabilityService availabilityService,
      PricingService pricingService) {
    this.hotelRepository = hotelRepository;
    this.availabilityService = availabilityService;
    this.pricingService = pricingService;
  }

  @Transactional(readOnly = true)
  public SmartAlternativesDto findAlternatives(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
    Hotel hotel =
        hotelRepository.findById(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
    int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    return new SmartAlternativesDto(
        nearestAvailable(hotel, checkIn, nights), cheapestAlternative(hotel, checkIn, checkOut));
  }

  private NearestAvailableDto nearestAvailable(Hotel hotel, LocalDate checkIn, int nights) {
    for (int shift = 1; shift <= MAX_SHIFT_DAYS; shift++) {
      LocalDate shiftedIn = checkIn.plusDays(shift);
      LocalDate shiftedOut = shiftedIn.plusDays(nights);
      int free = availabilityService.availableRooms(hotel, shiftedIn, shiftedOut);
      if (free > 0) {
        return new NearestAvailableDto(
            shiftedIn,
            shiftedOut,
            pricingService.nightsBetween(shiftedIn, shiftedOut),
            free,
            "Earliest dates with rooms for the same stay length");
      }
    }
    return null;
  }

  private CheapestAlternativeDto cheapestAlternative(
      Hotel requested, LocalDate checkIn, LocalDate checkOut) {
    List<Hotel> sameLocation =
        hotelRepository.findByLocationIgnoreCaseOrderByPricePerNightAsc(requested.getLocation());
    for (Hotel candidate : sameLocation) {
      if (candidate.getId().equals(requested.getId())) {
        continue;
      }
      int free = availabilityService.availableRooms(candidate, checkIn, checkOut);
      if (free > 0) {
        return new CheapestAlternativeDto(
            candidate.getId(),
            candidate.getName(),
            candidate.getLocation(),
            candidate.getPricePerNight(),
            free,
            true,
            checkIn,
            checkOut,
            "Cheapest hotel in " + candidate.getLocation() + " with rooms for your dates");
      }
    }
    return null;
  }
}
