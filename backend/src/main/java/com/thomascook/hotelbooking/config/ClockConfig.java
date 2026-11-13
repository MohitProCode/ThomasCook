package com.thomascook.hotelbooking.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {
  /** Injected clock so "past date" checks are testable with a fixed clock. */
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
