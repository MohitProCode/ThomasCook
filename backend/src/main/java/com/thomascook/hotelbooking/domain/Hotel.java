package com.thomascook.hotelbooking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Single room category per hotel: price/night + room count. Availability is always derived. */
@Entity
@Table(
    name = "hotels",
    uniqueConstraints =
        @UniqueConstraint(name = "uq_hotel_name_location", columnNames = {"name", "location"}),
    indexes = {
      @Index(name = "idx_hotels_location", columnList = "location"),
      @Index(name = "idx_hotels_location_price", columnList = "location,price_per_night"),
      @Index(name = "idx_hotels_source", columnList = "source,source_id"),
      @Index(name = "idx_hotels_geo", columnList = "latitude,longitude")
    })
public class Hotel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(nullable = false, length = 100)
  private String location;

  @Column(nullable = false, length = 2000)
  private String description;

  /** Comma-separated amenity names (MVP; a join table would be the v2 model). */
  @Column(nullable = false, length = 1000)
  private String amenities;

  @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerNight;

  @Column(name = "total_rooms", nullable = false)
  private int totalRooms;

  @Column(length = 40)
  private String source;

  @Column(name = "source_id", length = 120)
  private String sourceId;

  @Column(name = "property_type", length = 40)
  private String propertyType;

  @Column(length = 120)
  private String brand;

  @Column(length = 500)
  private String address;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(length = 80)
  private String phone;

  @Column(length = 500)
  private String website;

  @Column
  private Double latitude;

  @Column
  private Double longitude;

  // Nuitee content/rate metadata. These are nullable because the supplier may omit them.
  @Column(name = "main_photo", length = 1000)
  private String mainPhoto;

  @Column(name = "thumbnail", length = 1000)
  private String thumbnail;

  @Column(name = "guest_rating")
  private Double guestRating;

  @Column(name = "star_rating")
  private Double starRating;

  @Column(name = "review_count")
  private Integer reviewCount;

  @Column(length = 1000)
  private String tags;

  @Column(name = "room_name", length = 250)
  private String roomName;

  @Column(name = "rate_offer_id", length = 500)
  private String rateOfferId;

  @Column(name = "board_name", length = 120)
  private String boardName;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Version private Long version;

  protected Hotel() {}

  public Hotel(
      String name,
      String location,
      String description,
      String amenities,
      BigDecimal pricePerNight,
      int totalRooms) {
    this.name = name;
    this.location = location;
    this.description = description;
    this.amenities = amenities;
    this.pricePerNight = pricePerNight;
    this.totalRooms = totalRooms;
  }

  public List<String> getAmenityList() {
    if (amenities == null || amenities.isBlank()) {
      return List.of();
    }
    return Arrays.stream(amenities.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAmenities() {
    return amenities;
  }

  public void setAmenities(String amenities) {
    this.amenities = amenities;
  }

  public BigDecimal getPricePerNight() {
    return pricePerNight;
  }

  public void setPricePerNight(BigDecimal pricePerNight) {
    this.pricePerNight = pricePerNight;
  }

  public int getTotalRooms() {
    return totalRooms;
  }

  public void setTotalRooms(int totalRooms) {
    this.totalRooms = totalRooms;
  }

  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
  public String getSourceId() { return sourceId; }
  public void setSourceId(String sourceId) { this.sourceId = sourceId; }
  public String getPropertyType() { return propertyType; }
  public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
  public String getBrand() { return brand; }
  public void setBrand(String brand) { this.brand = brand; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getPostalCode() { return postalCode; }
  public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getWebsite() { return website; }
  public void setWebsite(String website) { this.website = website; }
  public Double getLatitude() { return latitude; }
  public void setLatitude(Double latitude) { this.latitude = latitude; }
  public Double getLongitude() { return longitude; }
  public void setLongitude(Double longitude) { this.longitude = longitude; }
  public String getMainPhoto() { return mainPhoto; }
  public void setMainPhoto(String mainPhoto) { this.mainPhoto = mainPhoto; }
  public String getThumbnail() { return thumbnail; }
  public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
  public Double getGuestRating() { return guestRating; }
  public void setGuestRating(Double guestRating) { this.guestRating = guestRating; }
  public Double getStarRating() { return starRating; }
  public void setStarRating(Double starRating) { this.starRating = starRating; }
  public Integer getReviewCount() { return reviewCount; }
  public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
  public String getTags() { return tags; }
  public void setTags(String tags) { this.tags = tags; }
  public String getRoomName() { return roomName; }
  public void setRoomName(String roomName) { this.roomName = roomName; }
  public String getRateOfferId() { return rateOfferId; }
  public void setRateOfferId(String rateOfferId) { this.rateOfferId = rateOfferId; }
  public String getBoardName() { return boardName; }
  public void setBoardName(String boardName) { this.boardName = boardName; }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
