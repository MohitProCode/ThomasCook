package com.thomascook.hotelbooking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "bookings",
    uniqueConstraints = @UniqueConstraint(name = "uq_booking_reference", columnNames = {"reference"}),
    indexes = {
      @Index(
          name = "idx_bookings_hotel_dates_status",
          columnList = "hotel_id,status,check_in,check_out"),
      @Index(name = "idx_bookings_email", columnList = "guest_email"),
      @Index(name = "idx_bookings_hotel_status", columnList = "hotel_id,status")
    })
public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Public lookup key, e.g. TC-7K2P9M. Generated server-side, never reused. */
  @Column(nullable = false, unique = true, length = 16)
  private String reference;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "hotel_id", nullable = false)
  private Hotel hotel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "room_offer_id", length = 500)
  private String roomOfferId;
  @Column(name = "room_name", length = 250)
  private String roomName;
  @Column(name = "guest_count")
  private Integer guestCount;

  @Column(name = "guest_name", nullable = false, length = 150)
  private String guestName;

  @Column(name = "guest_email", nullable = false, length = 254)
  private String guestEmail;

  @Column(name = "check_in", nullable = false)
  private LocalDate checkIn;

  @Column(name = "check_out", nullable = false)
  private LocalDate checkOut;

  @Column(nullable = false)
  private int nights;

  @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerNight;

  @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalPrice;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private BookingStatus status = BookingStatus.CONFIRMED;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  protected Booking() {}

  public Booking(
      String reference,
      Hotel hotel,
      String guestName,
      String guestEmail,
      LocalDate checkIn,
      LocalDate checkOut,
      int nights,
      BigDecimal pricePerNight,
      BigDecimal totalPrice) {
    this.reference = reference;
    this.hotel = hotel;
    this.guestName = guestName;
    this.guestEmail = guestEmail;
    this.checkIn = checkIn;
    this.checkOut = checkOut;
    this.nights = nights;
    this.pricePerNight = pricePerNight;
    this.totalPrice = totalPrice;
    this.status = BookingStatus.CONFIRMED;
  }

  public Long getId() {
    return id;
  }

  public String getReference() {
    return reference;
  }

  public Hotel getHotel() {
    return hotel;
  }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public String getRoomOfferId() { return roomOfferId; }
  public void setRoomOfferId(String roomOfferId) { this.roomOfferId = roomOfferId; }
  public String getRoomName() { return roomName; }
  public void setRoomName(String roomName) { this.roomName = roomName; }
  public Integer getGuestCount() { return guestCount; }
  public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

  public String getGuestName() {
    return guestName;
  }

  public String getGuestEmail() {
    return guestEmail;
  }

  public LocalDate getCheckIn() {
    return checkIn;
  }

  public LocalDate getCheckOut() {
    return checkOut;
  }

  public int getNights() {
    return nights;
  }

  public BigDecimal getPricePerNight() {
    return pricePerNight;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public void setStatus(BookingStatus status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getCancelledAt() {
    return cancelledAt;
  }

  public void setCancelledAt(LocalDateTime cancelledAt) {
    this.cancelledAt = cancelledAt;
  }
}
