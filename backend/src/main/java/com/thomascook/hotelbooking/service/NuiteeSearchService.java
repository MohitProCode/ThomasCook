package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.dto.HotelSummaryDto;
import com.thomascook.hotelbooking.dto.PageResponse;
import com.thomascook.hotelbooking.repository.HotelRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Searches and mirrors only live Nuitee rates into the local booking snapshot. */
@Service
public class NuiteeSearchService {
  private final NuiteeHotelProvider provider;
  private final HotelRepository hotels;
  private final AvailabilityService availability;
  private final PricingService pricing;
  private final String defaultPrice;

  public NuiteeSearchService(
      NuiteeHotelProvider provider, HotelRepository hotels, AvailabilityService availability,
      PricingService pricing, @Value("${inventory.default-price-per-night:3500}") String defaultPrice) {
    this.provider = provider;
    this.hotels = hotels;
    this.availability = availability;
    this.pricing = pricing;
    this.defaultPrice = defaultPrice;
  }

  @Transactional
  public PageResponse<HotelSummaryDto> search(
      String location, LocalDate checkIn, LocalDate checkOut, int guests, int page, int size) {
    if (!provider.isEnabled()) {
      throw new com.thomascook.hotelbooking.exception.InvalidRequestException(
          "Nuitee is not configured. Set NUITEE_ENABLED=true and NUITEE_API_KEY.");
    }
    List<NuiteeHotelProvider.ExternalHotel> external = provider.search(location, checkIn, checkOut, guests);
    List<HotelSummaryDto> content = external.stream().map(this::upsert).map(h -> toSummary(h, checkIn, checkOut)).toList();
    int safeSize = Math.min(Math.max(size, 1), 50);
    int safePage = Math.max(page, 0);
    int from = Math.min(safePage * safeSize, content.size());
    int to = Math.min(from + safeSize, content.size());
    return new PageResponse<>(content.subList(from, to), safePage, safeSize, content.size());
  }

  private synchronized Hotel upsert(NuiteeHotelProvider.ExternalHotel external) {
    Hotel hotel = external.sourceId() == null || external.sourceId().isBlank()
        ? hotels.findByNameAndLocation(external.name(), external.location()).orElse(null)
        : hotels.findBySourceAndSourceId("NUITEE", external.sourceId())
            .or(() -> hotels.findByNameAndLocation(external.name(), external.location()))
            .orElse(null);
    if (hotel == null) {
      hotel = new Hotel(external.name(), external.location(), external.description(),
          "WiFi,24-hour reception", external.pricePerNight(), Math.max(1, external.availableRooms()));
    }
    hotel.setName(trim(external.name(), 150));
    hotel.setLocation(trim(external.location(), 100));
    hotel.setDescription(trim(external.description(), 2000));
    hotel.setPricePerNight(external.pricePerNight() == null ? new BigDecimal(defaultPrice) : external.pricePerNight());
    hotel.setTotalRooms(Math.max(hotel.getTotalRooms(), Math.max(1, external.availableRooms())));
    hotel.setSource("NUITEE");
    hotel.setSourceId(trim(external.sourceId(), 120));
    hotel.setPropertyType("hotel");
    hotel.setBrand(trim(external.brand(), 120));
    hotel.setAddress(trim(external.address(), 500));
    hotel.setLatitude(external.latitude());
    hotel.setLongitude(external.longitude());
    hotel.setMainPhoto(trim(external.mainPhoto(), 1000));
    hotel.setThumbnail(trim(external.thumbnail(), 1000));
    hotel.setGuestRating(external.guestRating());
    hotel.setStarRating(external.starRating());
    hotel.setReviewCount(external.reviewCount());
    hotel.setRoomName(trim(external.roomName(), 250));
    hotel.setRateOfferId(trim(external.offerId(), 500));
    hotel.setBoardName(trim(external.boardName(), 120));
    return hotels.save(hotel);
  }

  private HotelSummaryDto toSummary(Hotel h, LocalDate checkIn, LocalDate checkOut) {
    int nights = pricing.nightsBetween(checkIn, checkOut);
    int free = availability.availableRooms(h, checkIn, checkOut);
    return new HotelSummaryDto(h.getId(), h.getName(), h.getLocation(), h.getDescription(), h.getAmenityList(),
        h.getPricePerNight(), h.getTotalRooms(), free, nights,
        pricing.totalForStay(h.getPricePerNight(), nights), h.getSource(), h.getPropertyType(), h.getBrand(),
        h.getAddress(), h.getPostalCode(), h.getPhone(), h.getWebsite(), h.getLatitude(), h.getLongitude(),
        h.getMainPhoto(), h.getThumbnail(), h.getGuestRating(), h.getStarRating(), h.getReviewCount(),
        h.getRoomName(), h.getRateOfferId(), h.getBoardName());
  }

  private String trim(String value, int max) {
    if (value == null || value.isBlank()) return null;
    return value.length() <= max ? value : value.substring(0, max);
  }
}
