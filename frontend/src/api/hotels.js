import { apiFetch } from './client.js';

function normalizeHotel(raw) {
  const availableRooms = raw.availableRooms ?? raw.roomsLeft ?? null;
  return {
    id: raw.id,
    name: raw.name,
    location: raw.location,
    description: raw.description,
    amenities: raw.amenities || [],
    pricePerNight: Number(raw.pricePerNight ?? 0),
    totalRooms: raw.totalRooms ?? null,
    availableRooms,
    available: availableRooms === null ? true : availableRooms > 0,
    roomsLeft: availableRooms,
    nights: raw.nights ?? null,
    totalForStay: raw.totalForStay != null ? Number(raw.totalForStay) : null,
    source: raw.source || null,
    propertyType: raw.propertyType || null,
    brand: raw.brand || null,
    address: raw.address || null,
    postalCode: raw.postalCode || null,
    phone: raw.phone || null,
    website: raw.website || null,
    latitude: raw.latitude ?? null,
    longitude: raw.longitude ?? null,
    mainPhoto: raw.mainPhoto || null,
    thumbnail: raw.thumbnail || null,
    guestRating: raw.guestRating ?? null,
    starRating: raw.starRating ?? null,
    reviewCount: raw.reviewCount ?? null,
    roomName: raw.roomName || null,
    boardName: raw.boardName || null,
    images: raw.images || [],
  };
}

function normalizeAlternatives(raw) {
  if (!raw) return null;
  const nearest = raw.nearestAvailable || raw.sameHotelNextAvailable || null;
  const cheapest =
    raw.cheapestAlternative || raw.sameLocationCheapest || null;
  if (!nearest && !cheapest) return null;
  return {
    nearestAvailable: nearest
      ? {
          checkIn: nearest.checkIn,
          checkOut: nearest.checkOut,
          nights: nearest.nights ?? null,
          availableRooms: nearest.availableRooms ?? null,
          message: nearest.message || '',
        }
      : null,
    cheapestAlternative: cheapest
      ? {
          hotelId: cheapest.hotelId ?? cheapest.id ?? null,
          name: cheapest.name || '',
          location: cheapest.location || '',
          pricePerNight: Number(cheapest.pricePerNight ?? 0),
          availableRooms: cheapest.availableRooms ?? null,
          available: cheapest.available ?? true,
          checkIn: cheapest.checkIn || null,
          checkOut: cheapest.checkOut || null,
          message: cheapest.message || '',
        }
      : null,
  };
}

export async function searchHotels({ location, checkIn, checkOut, guests = 2 }) {
  const endpoint = '/api/hotels';
  const data = await apiFetch(endpoint, {
    query: { location, checkIn, checkOut, guests, size: 50 },
  });
  const list = data?.content ?? data?.results ?? [];
  return {
    results: list.map(normalizeHotel),
    total: data?.totalElements ?? list.length,
    alternatives: normalizeAlternatives(data?.smartAlternatives || data?.alternatives),
  };
}

export async function getHotel(id, { checkIn, checkOut } = {}) {
  const data = await apiFetch(`/api/hotels/${id}`, { query: { checkIn, checkOut } });
  const hotel = normalizeHotel(data);
  return {
    ...hotel,
    availability: data?.availability || null,
    alternatives: normalizeAlternatives(data?.smartAlternatives || data?.alternatives),
  };
}

export async function getQuote(id, { checkIn, checkOut }) {
  return apiFetch(`/api/hotels/${id}/quote`, {
    method: 'POST',
    body: { checkIn, checkOut },
  });
}

