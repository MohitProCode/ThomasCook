package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.domain.Booking;
import com.thomascook.hotelbooking.domain.BookingStatus;
import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.dto.BookingResponse;
import com.thomascook.hotelbooking.dto.CancelBookingResponse;
import com.thomascook.hotelbooking.dto.CreateBookingRequest;
import com.thomascook.hotelbooking.dto.SmartAlternativesDto;
import com.thomascook.hotelbooking.exception.BookingAlreadyCancelledException;
import com.thomascook.hotelbooking.exception.BookingNotFoundException;
import com.thomascook.hotelbooking.exception.HotelNotFoundException;
import com.thomascook.hotelbooking.exception.InvalidRequestException;
import com.thomascook.hotelbooking.exception.SoldOutException;
import com.thomascook.hotelbooking.repository.BookingRepository;
import com.thomascook.hotelbooking.repository.HotelRepository;
import com.thomascook.hotelbooking.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

  private final HotelRepository hotelRepository;
  private final UserRepository userRepository;
  private final BookingRepository bookingRepository;
  private final AvailabilityService availabilityService;
  private final SmartAlternativesService smartAlternativesService;
  private final PricingService pricingService;
  private final BookingReferenceGenerator referenceGenerator;
  private final DateRangeValidator dateRangeValidator;
  private final Clock clock;
  private final String currency;

  public BookingService(
      HotelRepository hotelRepository,
      UserRepository userRepository,
      BookingRepository bookingRepository,
      AvailabilityService availabilityService,
      SmartAlternativesService smartAlternativesService,
      PricingService pricingService,
      BookingReferenceGenerator referenceGenerator,
      DateRangeValidator dateRangeValidator,
      Clock clock,
      @Value("${app.currency:INR}") String currency) {
    this.hotelRepository = hotelRepository;
    this.userRepository = userRepository;
    this.bookingRepository = bookingRepository;
    this.availabilityService = availabilityService;
    this.smartAlternativesService = smartAlternativesService;
    this.pricingService = pricingService;
    this.referenceGenerator = referenceGenerator;
    this.dateRangeValidator = dateRangeValidator;
    this.clock = clock;
    this.currency = currency;
  }

  /**
   * Creates one CONFIRMED booking. The overlap-count-then-insert sequence is a check-then-act race,
   * so the hotel row is pessimistically locked first: concurrent bookings for the same hotel
   * serialize here while other hotels stay unaffected. (A unique constraint cannot express "at
   * most N overlapping rows", and the @Version field only guards explicit hotel edits, so neither
   * replaces this lock.)
   */
  @Transactional
  public BookingResponse create(CreateBookingRequest request) {
    Hotel hotel =
        hotelRepository
            .findByIdForUpdate(request.getHotelId())
            .filter(h -> "NUITEE".equalsIgnoreCase(h.getSource()))
            .orElseThrow(() -> new HotelNotFoundException(request.getHotelId()));
    dateRangeValidator.validate(request.getCheckIn(), request.getCheckOut());

    int free =
        availabilityService.availableRooms(hotel, request.getCheckIn(), request.getCheckOut());
    if (free <= 0) {
      SmartAlternativesDto alternatives =
          smartAlternativesService.findAlternatives(
              hotel.getId(), request.getCheckIn(), request.getCheckOut());
      throw new SoldOutException("Selected dates are sold out for this hotel", alternatives);
    }

    int nights = pricingService.nightsBetween(request.getCheckIn(), request.getCheckOut());
    BigDecimal total = pricingService.totalForStay(hotel.getPricePerNight(), nights);
    String email = request.getEmail().trim().toLowerCase();
    String guestName = request.getGuestName().trim();

    Booking booking =
        new Booking(
            nextUniqueReference(),
            hotel,
            guestName,
            email,
            request.getCheckIn(),
            request.getCheckOut(),
            nights,
            hotel.getPricePerNight(),
            total);
    String authenticatedEmail = authenticatedEmail();
    if (authenticatedEmail != null) {
      booking.setUser(userRepository.findByEmailIgnoreCase(authenticatedEmail)
          .orElseThrow(() -> new InvalidRequestException("Authenticated account was not found")));
    }
    try {
      booking = bookingRepository.saveAndFlush(booking);
    } catch (DataIntegrityViolationException collision) {
      // Extremely rare reference collision: retry once with a fresh reference.
      booking =
          new Booking(
              nextUniqueReference(),
              hotel,
              guestName,
              email,
              request.getCheckIn(),
              request.getCheckOut(),
              nights,
              hotel.getPricePerNight(),
              total);
      booking = bookingRepository.saveAndFlush(booking);
    }
    return toResponse(booking);
  }

  @Transactional(readOnly = true)
  public BookingResponse lookup(String reference, String email) {
    return toResponse(requireOwned(reference, email));
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> mine() {
    String email = authenticatedEmail();
    if (email == null) throw new InvalidRequestException("Authentication is required");
    return bookingRepository.findByUserEmailIgnoreCaseOrderByCreatedAtDesc(email).stream().map(this::toResponse).toList();
  }

  @Transactional
  public CancelBookingResponse cancel(String reference, String email) {
    Booking booking = requireOwned(reference, email);
    if (booking.getStatus() == BookingStatus.CANCELLED) {
      throw new BookingAlreadyCancelledException();
    }
    booking.setStatus(BookingStatus.CANCELLED);
    booking.setCancelledAt(LocalDateTime.now(clock));
    bookingRepository.save(booking);
    return new CancelBookingResponse(
        booking.getReference(),
        booking.getStatus(),
        booking.getCancelledAt(),
        "Booking cancelled. Availability has been released.");
  }

  private Booking requireOwned(String reference, String email) {
    if (reference == null || reference.isBlank()) {
      throw new InvalidRequestException("Booking reference is required");
    }
    String authenticatedEmail = authenticatedEmail();
    if (authenticatedEmail == null && (email == null || email.isBlank())) {
      throw new InvalidRequestException("Email is required");
    }
    Booking booking = authenticatedEmail == null
        ? bookingRepository.findByReferenceIgnoreCase(reference.trim()).orElseThrow(BookingNotFoundException::new)
        : bookingRepository.findByReferenceIgnoreCaseAndUserEmailIgnoreCase(reference.trim(), authenticatedEmail).orElseThrow(BookingNotFoundException::new);
    if (authenticatedEmail == null && !booking.getGuestEmail().equalsIgnoreCase(email.trim())) {
      throw new BookingNotFoundException();
    }
    return booking;
  }

  private String authenticatedEmail() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String
        ? ((String) auth.getPrincipal()).trim().toLowerCase() : null;
  }

  private String nextUniqueReference() {
    for (int attempt = 0; attempt < 5; attempt++) {
      String candidate = referenceGenerator.generate();
      if (!bookingRepository.existsByReferenceIgnoreCase(candidate)) {
        return candidate;
      }
    }
    return referenceGenerator.generate();
  }

  private BookingResponse toResponse(Booking booking) {
    Hotel hotel = booking.getHotel();
    return new BookingResponse(
        booking.getReference(),
        hotel.getId(),
        hotel.getName(),
        hotel.getLocation(),
        booking.getGuestName(),
        booking.getGuestEmail(),
        booking.getGuestEmail(),
        booking.getCheckIn(),
        booking.getCheckOut(),
        booking.getNights(),
        booking.getPricePerNight(),
        booking.getTotalPrice(),
        booking.getTotalPrice(),
        currency,
        booking.getStatus(),
        booking.getCreatedAt(),
        booking.getCancelledAt());
  }
}
