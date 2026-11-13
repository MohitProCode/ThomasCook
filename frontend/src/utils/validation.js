import { nightsBetween, todayISO } from './dates.js';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function isEmail(value) {
  return EMAIL_RE.test(String(value || '').trim());
}

export function validateSearch({ location, checkIn, checkOut, guests }) {
  const errors = {};
  if (!location || location.trim().length < 2) {
    errors.location = 'Enter a destination (at least 2 characters).';
  }
  if (!checkIn) {
    errors.checkIn = 'Choose a check-in date.';
  } else if (checkIn < todayISO()) {
    errors.checkIn = 'Check-in date cannot be in the past.';
  }
  if (!checkOut) {
    errors.checkOut = 'Choose a check-out date.';
  }
  if (checkIn && checkOut) {
    const nights = nightsBetween(checkIn, checkOut);
    if (nights < 1) {
      errors.checkOut = 'Check-out date must be after check-in date.';
    } else if (nights > 30) {
      errors.checkOut = 'Stays are limited to 30 nights.';
    }
  }
  if (!Number.isInteger(guests) || guests < 1 || guests > 6) {
    errors.guests = 'Guests must be between 1 and 6.';
  }
  return errors;
}

export function validateBooking({ guestName, email }) {
  const errors = {};
  if (!guestName || guestName.trim().length < 2) {
    errors.guestName = 'Enter the full guest name (at least 2 characters).';
  }
  if (!email || !email.trim()) {
    errors.email = 'Enter an email address.';
  } else if (!isEmail(email)) {
    errors.email = 'Enter a valid email address.';
  }
  return errors;
}

export function validateLookup({ reference, email }) {
  const errors = {};
  if (!reference || reference.trim().length < 6) {
    errors.reference = 'Enter your booking reference (e.g. TC-7K2P9M).';
  }
  if (!email || !email.trim()) {
    errors.email = 'Enter the email used for the booking.';
  } else if (!isEmail(email)) {
    errors.email = 'Enter a valid email address.';
  }
  return errors;
}
