package com.thomascook.hotelbooking.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Service;

/** Generates public booking references like TC-7K2P9M (no ambiguous 0/O/1/I/L chars). */
@Service
public class BookingReferenceGenerator {

  static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
  private static final String PREFIX = "TC-";
  private static final int CODE_LENGTH = 6;

  private final SecureRandom random = new SecureRandom();

  public String generate() {
    StringBuilder code = new StringBuilder(CODE_LENGTH);
    for (int i = 0; i < CODE_LENGTH; i++) {
      code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return PREFIX + code;
  }
}
