package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.repository.HotelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Removes all non-Nuitee hotel snapshots on startup; booking history is preserved. */
@Component
public class NuiteeInventoryReconciler implements CommandLineRunner {
  private final HotelRepository hotels;

  public NuiteeInventoryReconciler(HotelRepository hotels) {
    this.hotels = hotels;
  }

  @Override
  @Transactional
  public void run(String... args) {
    hotels.deleteUnbookedOutsideSource("NUITEE");
  }
}
