package com.thomascook.hotelbooking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thomascook.hotelbooking.domain.Hotel;
import com.thomascook.hotelbooking.dto.HotelContentDto;
import com.thomascook.hotelbooking.dto.HotelDetailDto;
import com.thomascook.hotelbooking.exception.InvalidRequestException;
import com.thomascook.hotelbooking.repository.HotelRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HotelServiceTest {

  @Autowired private HotelService hotelService;
  @Autowired private HotelRepository hotelRepository;
  @MockBean private NuiteeHotelProvider nuiteeProvider;

  private Hotel nuiteeHotel;

  @BeforeEach
  void seed() {
    hotelRepository.deleteAll();
    nuiteeHotel =
        new Hotel(
            "Casa Nuitee",
            "Goa",
            "Live hotel inventory from Nuitee Connect.",
            "WiFi",
            new BigDecimal("5000.00"),
            10);
    nuiteeHotel.setSource("NUITEE");
    nuiteeHotel.setSourceId("NUITEE-1");
    nuiteeHotel = hotelRepository.save(nuiteeHotel);
  }

  private HotelContentDto content(List<String> amenities, List<String> images) {
    return new HotelContentDto(
        "NUITEE-1",
        "Casa Nuitee",
        "Full description",
        "Beach Road",
        "Goa",
        "IN",
        15.5,
        73.8,
        "https://img/main.jpg",
        "https://img/thumb.jpg",
        amenities,
        images,
        List.of());
  }

  @Test
  void detail_merges_nuitee_content_images_and_facilities() {
    when(nuiteeProvider.isEnabled()).thenReturn(true);
    when(nuiteeProvider.content("NUITEE-1"))
        .thenReturn(content(List.of("Pool", "Spa"), List.of("https://img/1.jpg", "https://img/2.jpg")));

    HotelDetailDto detail = hotelService.getDetail(nuiteeHotel.getId(), null, null);

    assertThat(detail.images()).containsExactly("https://img/1.jpg", "https://img/2.jpg");
    assertThat(detail.amenities()).containsExactly("Pool", "Spa");
  }

  @Test
  void detail_falls_back_to_snapshot_when_content_fails() {
    when(nuiteeProvider.isEnabled()).thenReturn(true);
    when(nuiteeProvider.content("NUITEE-1"))
        .thenThrow(new InvalidRequestException("Nuitee hotel details failed (HTTP 500)"));

    HotelDetailDto detail = hotelService.getDetail(nuiteeHotel.getId(), null, null);

    assertThat(detail.amenities()).containsExactly("WiFi");
    assertThat(detail.images()).isNull();
  }

  @Test
  void detail_skips_content_when_provider_disabled() {
    when(nuiteeProvider.isEnabled()).thenReturn(false);

    HotelDetailDto detail = hotelService.getDetail(nuiteeHotel.getId(), null, null);

    verify(nuiteeProvider, never()).content(anyString());
    assertThat(detail.amenities()).containsExactly("WiFi");
    assertThat(detail.images()).isNull();
  }
}
