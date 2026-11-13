import { apiFetch } from './client.js';

export function normalizeBooking(raw) {
  if (!raw) return null;
  return {
    reference: raw.reference,
    hotelId: raw.hotelId,
    hotelName: raw.hotelName || raw.name || '',
    location: raw.location || '',
    guestName: raw.guestName || '',
    email: raw.email || raw.guestEmail || '',
    checkIn: raw.checkIn,
    checkOut: raw.checkOut,
    nights: raw.nights ?? null,
    pricePerNight: Number(raw.pricePerNight ?? 0),
    total: Number(raw.totalPrice ?? raw.totalAmount ?? raw.total ?? 0),
    currency: raw.currency || 'INR',
    status: raw.status || 'CONFIRMED',
    createdAt: raw.createdAt || null,
    cancelledAt: raw.cancelledAt || null,
  };
}

export async function createBooking({ hotelId, checkIn, checkOut, guestName, email }) {
  const data = await apiFetch('/api/bookings', {
    method: 'POST',
    body: { hotelId, checkIn, checkOut, guestName, email },
  });
  return normalizeBooking(data);
}

export async function getBooking(reference, email) {
  const data = await apiFetch('/api/bookings/lookup', {
    query: { reference: reference?.trim(), email: email?.trim() },
  });
  return normalizeBooking(data);
}

export async function getMyBookings() {
  const data = await apiFetch('/api/bookings/mine');
  return (data || []).map(normalizeBooking);
}

export async function cancelBooking(reference, email) {
  return apiFetch(`/api/bookings/${encodeURIComponent(reference?.trim())}/cancel`, {
    method: 'POST',
    query: { email: email?.trim() },
  });
}
