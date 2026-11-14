package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.dto.AvailabilityDto;
import com.thomascook.hotelbooking.dto.AvailabilityCalendarDto;
import com.thomascook.hotelbooking.dto.AvailabilityDayDto;
import com.thomascook.hotelbooking.dto.HotelContentDto;
import com.thomascook.hotelbooking.dto.HotelDetailDto;
import com.thomascook.hotelbooking.dto.HotelSummaryDto;
import com.thomascook.hotelbooking.dto.PageResponse;
import com.thomascook.hotelbooking.dto.QuoteResponse;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto;
import com.thomascook.hotelbooking.exception.HotelNotFoundException;
import com.thomascook.hotelbooking.exception.InvalidRequestException;
import com.thomascook.hotelbooking.repository.HotelRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HotelService {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HotelService.class);

  private final HotelRepository hotelRepository;
  private final AvailabilityService availabilityService;
  private final SmartAlternativesService smartAlternativesService;
  private final PricingService pricingService;
  private final DateRangeValidator dateRangeValidator;
  private final NuiteeHotelProvider nuiteeProvider;
  private final String currency;

  public HotelService(
      HotelRepository hotelRepository,
      AvailabilityService availabilityService,
      SmartAlternativesService smartAlternativesService,
      PricingService pricingService,
      DateRangeValidator dateRangeValidator,
      NuiteeHotelProvider nuiteeProvider,
      @Value("${app.currency:INR}") String currency) {
    this.hotelRepository = hotelRepository;
    this.availabilityService = availabilityService;
    this.smartAlternativesService = smartAlternativesService;
    this.pricingService = pricingService;
    this.dateRangeValidator = dateRangeValidator;
    this.nuiteeProvider = nuiteeProvider;
    this.currency = currency;
  }

  @Transactional(readOnly = true)
  public PageResponse<HotelSummaryDto> search(
      String location, LocalDate checkIn, LocalDate checkOut, int page, int size) {
    dateRangeValidator.validateOptionalPair(checkIn, checkOut);
    int safeSize = Math.min(Math.max(size, 1), 50);
    int safePage = Math.max(page, 0);

    List<Hotel> hotels =
        location == null || location.isBlank()
            ? hotelRepository.findBySourceOrderByPricePerNightAsc("NUITEE")
            : hotelRepository.findBySourceAndLocationContainingIgnoreCaseOrderByPricePerNightAsc(
                "NUITEE", location.trim());

    Integer nights = checkIn != null ? pricingService.nightsBetween(checkIn, checkOut) : null;
    List<HotelSummaryDto> content =
        hotels.stream().map(h -> toSummary(h, checkIn, checkOut, nights)).toList();

    int from = Math.min(safePage * safeSize, content.size());
    int to = Math.min(from + safeSize, content.size());
    return new PageResponse<>(content.subList(from, to), safePage, safeSize, content.size());
  }

  @Transactional(readOnly = true)
  public HotelDetailDto getDetail(Long id, LocalDate checkIn, LocalDate checkOut) {
    dateRangeValidator.validateOptionalPair(checkIn, checkOut);
    Hotel hotel = hotelRepository.findById(id)
        .filter(h -> "NUITEE".equalsIgnoreCase(h.getSource()))
        .orElseThrow(() -> new HotelNotFoundException(id));
    HotelContentDto content = fetchContent(hotel);
    if (checkIn == null) {
      return toDetail(hotel, null, null, null, content);
    }
    int free = availabilityService.availableRooms(hotel, checkIn, checkOut);
    int nights = pricingService.nightsBetween(checkIn, checkOut);
    AvailabilityDto availability = new AvailabilityDto(checkIn, checkOut, nights, free);
    SmartAlternativesDto alternatives =
        free == 0 ? smartAlternativesService.findAlternatives(id, checkIn, checkOut) : null;
    return toDetail(hotel, free, availability, alternatives, content);
  }

  /** Live Nuitee content enriches the stored snapshot; any failure degrades to the snapshot. */
  private HotelContentDto fetchContent(Hotel hotel) {
    if (!nuiteeProvider.isEnabled() || hotel.getSourceId() == null || hotel.getSourceId().isBlank()) {
      return null;
    }
    try {
      return nuiteeProvider.content(hotel.getSourceId());
    } catch (RuntimeException ex) {
      log.warn("Nuitee content fetch failed for hotel {}; serving stored snapshot", hotel.getId(), ex);
      return null;
    }
  }

  @Transactional(readOnly = true)
  public QuoteResponse quote(Long id, LocalDate checkIn, LocalDate checkOut) {
    dateRangeValidator.validate(checkIn, checkOut);
    Hotel hotel = hotelRepository.findById(id)
        .filter(h -> "NUITEE".equalsIgnoreCase(h.getSource()))
        .orElseThrow(() -> new HotelNotFoundException(id));
    int nights = pricingService.nightsBetween(checkIn, checkOut);
    BigDecimal total = pricingService.totalForStay(hotel.getPricePerNight(), nights);
    int free = availabilityService.availableRooms(hotel, checkIn, checkOut);
    return new QuoteResponse(
        hotel.getId(), hotel.getPricePerNight(), nights, total, currency, free);
  }

  @Transactional(readOnly = true)
  public AvailabilityCalendarDto availabilityCalendar(Long id, LocalDate from, LocalDate to) {
    Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new HotelNotFoundException(id));
    if ("NUITEE".equalsIgnoreCase(hotel.getSource())) {
      throw new InvalidRequestException(
          "Nuitee supplies availability for the selected check-in and check-out range, not a daily room calendar");
    }
    LocalDate start = from == null ? LocalDate.now() : from;
    LocalDate end = to == null ? start.plusDays(30) : to;
    long dayCount = ChronoUnit.DAYS.between(start, end);
    if (dayCount < 1 || dayCount > 366) {
      throw new InvalidRequestException("Availability calendar must cover between 1 and 366 days");
    }
    List<AvailabilityDayDto> days =
        start.datesUntil(end).map(date -> availabilityDay(hotel, date)).toList();
    return new AvailabilityCalendarDto(id, start, end, days);
  }

  private AvailabilityDayDto availabilityDay(Hotel hotel, LocalDate date) {
    int available = availabilityService.availableRooms(hotel, date, date.plusDays(1));
    int occupied = hotel.getTotalRooms() == 0
        ? 100
        : (int) Math.round((hotel.getTotalRooms() - available) * 100.0 / hotel.getTotalRooms());
    String status = available == 0 ? "SOLD_OUT" : occupied >= 80 ? "ALMOST_FULL" : "AVAILABLE";
    return new AvailabilityDayDto(date, available, hotel.getTotalRooms(), occupied, status);
  }

  private HotelSummaryDto toSummary(
      Hotel hotel, LocalDate checkIn, LocalDate checkOut, Integer nights) {
    Integer free = null;
    BigDecimal totalForStay = null;
    if (checkIn != null && nights != null) {
      free = availabilityService.availableRooms(hotel, checkIn, checkOut);
      totalForStay = pricingService.totalForStay(hotel.getPricePerNight(), nights);
    }
    return new HotelSummaryDto(
        hotel.getId(),
        hotel.getName(),
        hotel.getLocation(),
        hotel.getDescription(),
        hotel.getAmenityList(),
        hotel.getPricePerNight(),
        hotel.getTotalRooms(),
        free,
        nights,
        totalForStay,
        hotel.getSource(), hotel.getPropertyType(), hotel.getBrand(), hotel.getAddress(),
        hotel.getPostalCode(), hotel.getPhone(), hotel.getWebsite(), hotel.getLatitude(), hotel.getLongitude(),
        hotel.getMainPhoto(), hotel.getThumbnail(), hotel.getGuestRating(), hotel.getStarRating(), hotel.getReviewCount(),
        hotel.getRoomName(), hotel.getRateOfferId(), hotel.getBoardName());
  }

  private HotelDetailDto toDetail(
      Hotel hotel,
      Integer free,
      AvailabilityDto availability,
      SmartAlternativesDto alternatives,
      HotelContentDto content) {
    return new HotelDetailDto(
        hotel.getId(),
        hotel.getName(),
        hotel.getLocation(),
        hotel.getDescription(),
        contentAmenities(hotel, content),
        hotel.getPricePerNight(),
        hotel.getTotalRooms(),
        free,
        availability,
        alternatives,
        hotel.getSource(), hotel.getPropertyType(), hotel.getBrand(), hotel.getAddress(),
        hotel.getPostalCode(), hotel.getPhone(), hotel.getWebsite(), hotel.getLatitude(), hotel.getLongitude(),
        hotel.getMainPhoto(), hotel.getThumbnail(), hotel.getGuestRating(), hotel.getStarRating(), hotel.getReviewCount(),
        hotel.getRoomName(), hotel.getRateOfferId(), hotel.getBoardName(), contentImages(content));
  }

  private List<String> contentAmenities(Hotel hotel, HotelContentDto content) {
    if (content != null && content.amenities() != null && !content.amenities().isEmpty()) {
      return content.amenities();
    }
    return hotel.getAmenityList();
  }

  private List<String> contentImages(HotelContentDto content) {
    if (content != null && content.images() != null && !content.images().isEmpty()) {
      return List.copyOf(content.images());
    }
    return null;
  }
}
