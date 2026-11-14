import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getHotel } from '../api/hotels.js';
import { createBooking } from '../api/bookings.js';
import { ApiError } from '../api/client.js';
import { useQueryParams } from '../hooks/useQueryParams.js';
import { useFetch } from '../hooks/useFetch.js';
import { useBooking } from '../context/BookingContext.jsx';
import { isValidRange, nightsBetween } from '../utils/dates.js';
import { formatMoney } from '../utils/money.js';
import { AvailabilityBadge } from '../components/HotelCard.jsx';
import AlternativesPanel from '../components/AlternativesPanel.jsx';
import PhotoCarousel from '../components/PhotoCarousel.jsx';
import PriceBreakdown from '../components/PriceBreakdown.jsx';
import BookingForm from '../components/BookingForm.jsx';
import LoadingSkeleton from '../components/LoadingSkeleton.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';
import { useAuth } from '../context/AuthContext.jsx';

const AMENITY_ICONS = {
  'WiFi': '📶', 'Pool': '🏊', 'Spa': '💆', 'Gym': '🏋️', 'Restaurant': '🍽️',
  'Bar': '🍸', 'Parking': '🅿️', 'AC': '❄️', 'Beach access': '🏖️',
  'Room service': '🛎️', 'Concierge': '🎩', 'Laundry': '👕',
};

const GALLERY_IMGS = [
  'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=600&q=75',
  'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=600&q=75',
  'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=600&q=75',
  'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=600&q=75',
];

export default function HotelDetailPage() {
  const { id } = useParams();
  const query = useQueryParams();
  const navigate = useNavigate();
  const { saveBooking } = useBooking();
  const { user } = useAuth();
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [soldOut, setSoldOut] = useState(null);
  const [amenitiesExpanded, setAmenitiesExpanded] = useState(false);

  const datesValid = isValidRange(query.checkIn, query.checkOut);
  const nights = nightsBetween(query.checkIn, query.checkOut);

  const { status, data: hotel, error, retry } = useFetch(
    () => getHotel(id, { checkIn: query.checkIn, checkOut: query.checkOut }),
    [id, query.checkIn, query.checkOut],
  );

  const heroSrc = hotel?.mainPhoto || hotel?.thumbnail || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1600&q=85';
  const realPhotos = [...new Set([...(hotel?.images || []), hotel?.mainPhoto, hotel?.thumbnail].filter(Boolean))].slice(0, 8);
  const galleryPhotos = realPhotos.length > 0 ? realPhotos : GALLERY_IMGS;
  const guestScore = hotel?.guestRating > 0 ? hotel.guestRating : null;
  const starClass = hotel?.starRating > 0 ? hotel.starRating : null;
  const mapUrl = hotel?.latitude != null && hotel?.longitude != null
    ? `https://www.google.com/maps/search/?api=1&query=${hotel.latitude},${hotel.longitude}`
    : null;
  const uniqueAmenities = [...new Map((hotel?.amenities || []).map((a) => [a.toLowerCase(), a])).values()];

  async function handleBooking({ guestName, email }) {
    setSubmitting(true);
    setFormError('');
    try {
      const booking = await createBooking({
        hotelId: Number(id),
        checkIn: query.checkIn,
        checkOut: query.checkOut,
        guestName,
        email,
      });
      saveBooking(booking);
      navigate(`/confirmation/${booking.reference}`, { state: { booking } });
    } catch (err) {
      if (err instanceof ApiError && err.status === 409 && err.alternatives) {
        setSoldOut(err.alternatives);
      } else {
        setFormError(err?.message || 'Booking failed. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (!datesValid) {
    return (
      <section>
        <ErrorBanner message="Pick valid dates to see availability and book." />
        <p><Link to="/" className="btn btn-secondary">← Back to search</Link></p>
      </section>
    );
  }

  return (
    <section>
      {status === 'loading' && <LoadingSkeleton rows={2} />}
      {status === 'error' && (
        <>
          <ErrorBanner message={error?.message || 'Could not load this hotel.'} onRetry={retry} />
          <p><Link to="/results" className="btn btn-secondary">← Back to results</Link></p>
        </>
      )}

      {status === 'success' && hotel && (
        <>
          {/* Hero image — full bleed */}
          <div className="detail-hero">
            <img
              className="detail-hero-img"
              src={heroSrc}
              alt={hotel.name}
            />
            <div className="detail-hero-overlay">
              <p className="eyebrow" style={{ color: 'rgba(255,255,255,0.55)', marginBottom: 'var(--space-3)', letterSpacing: '0.12em' }}>
                {hotel.location}
              </p>
              <h1>{hotel.name}</h1>
              {(starClass != null || guestScore != null || hotel.address) && (
                <p style={{ color: 'rgba(255,255,255,0.85)', fontSize: '.9rem', marginTop: 'var(--space-2)' }}>
                  {starClass != null && <span style={{ letterSpacing: '.1em' }}>{'★'.repeat(Math.min(5, Math.round(starClass)))}</span>}
                  {starClass != null && guestScore != null && ' · '}
                  {guestScore != null && <span>{Number(guestScore).toFixed(1)}{hotel.reviewCount > 0 && ` (${hotel.reviewCount.toLocaleString()} reviews)`}</span>}
                  {(starClass != null || guestScore != null) && hotel.address && ' · '}
                  {hotel.address && <span>{hotel.address}</span>}
                </p>
              )}
            </div>
          </div>

          {/* Gallery strip */}
          {galleryPhotos.length > 1 && (
            <div className="detail-gallery-strip">
              {galleryPhotos.map((src, i) => (
                <div
                  className="gallery-thumb"
                  key={i}
                  style={galleryPhotos.length < 4 ? { gridColumn: `span ${4 / galleryPhotos.length}` } : undefined}
                >
                  <img src={src} alt={`${hotel.name} photo ${i + 1}`} loading="lazy" />
                </div>
              ))}
            </div>
          )}
          <PhotoCarousel photos={galleryPhotos} name={hotel.name} />

          <p style={{ marginBottom: '1rem' }}>
            <Link
              to={`/results?location=${encodeURIComponent(hotel.location)}&checkIn=${query.checkIn}&checkOut=${query.checkOut}&guests=${query.guests}`}
              style={{ fontSize: '.88rem', color: 'var(--muted)' }}
            >
              ← Back to results
            </Link>
          </p>

          <div className="detail-layout">
            {/* Left column */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '.75rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
                <AvailabilityBadge hotel={hotel} />
                {hotel.pricePerNight && (
                  <span className="muted small">from {formatMoney(hotel.pricePerNight)} / night</span>
                )}
              </div>

              <div className="detail-section">
                <h2>About this property</h2>
                <p style={{ lineHeight: 1.7, color: 'var(--text-2)' }}>{hotel.description}</p>
              </div>

              {(hotel.address || hotel.brand || hotel.propertyType || hotel.phone || hotel.website || mapUrl) && (
                <div className="detail-section">
                  <h2>Property details</h2>
                  <div style={{ display: 'grid', gap: '.4rem', fontSize: '.9rem', color: 'var(--text-2)' }}>
                    {hotel.address && (
                      <p>
                        <strong>Address:</strong> {hotel.address}{hotel.postalCode ? ` ${hotel.postalCode}` : ''}
                        {mapUrl && <> · <a href={mapUrl} target="_blank" rel="noreferrer">View on map</a></>}
                      </p>
                    )}
                    {(hotel.brand || hotel.propertyType) && (
                      <p><strong>Property:</strong> {[hotel.brand, hotel.propertyType].filter(Boolean).join(' · ')}</p>
                    )}
                    {hotel.phone && <p><strong>Phone:</strong> {hotel.phone}</p>}
                    {hotel.website && <p><strong>Website:</strong> <a href={hotel.website} target="_blank" rel="noreferrer">{hotel.website}</a></p>}
                  </div>
                </div>
              )}

              {hotel.amenities?.length > 0 && (
                <div className="detail-section">
                  <h2>Amenities</h2>
                  <div className="amenity-grid">
                    {(amenitiesExpanded ? uniqueAmenities : uniqueAmenities.slice(0, 8)).map((amenity) => (
                      <div key={amenity} className="amenity-item">
                        <span className="amenity-icon">{AMENITY_ICONS[amenity] || '✓'}</span>
                        {amenity}
                      </div>
                    ))}
                  </div>
                  {uniqueAmenities.length > 8 && (
                    <button
                      type="button"
                      className="btn btn-ghost btn-small"
                      aria-expanded={amenitiesExpanded}
                      onClick={() => setAmenitiesExpanded((v) => !v)}
                      style={{ marginTop: '.75rem' }}
                    >
                      {amenitiesExpanded ? 'Show fewer amenities' : `Show all ${uniqueAmenities.length} amenities`}
                    </button>
                  )}
                </div>
              )}

              <div className="availability-panel card detail-section">
                <p className="eyebrow" style={{ color: 'var(--brand)', background: 'var(--brand-light)', border: '1px solid var(--brand-light)', borderRadius: 'var(--radius-full)', display: 'inline-flex', padding: '.25rem .75rem', marginBottom: '.75rem' }}>
                  LIVE INVENTORY
                </p>
                <h2 style={{ marginBottom: '.5rem' }}>Availability for your stay</h2>
                <p className="muted" style={{ fontSize: '.88rem' }}>
                  Rates and room availability are checked for {query.checkIn} → {query.checkOut} ({nights} night{nights === 1 ? '' : 's'}).
                </p>
                {(hotel.roomName || hotel.boardName) && (
                  <p style={{ fontSize: '.88rem', marginTop: '.5rem' }}>
                    <strong>Your room:</strong> {[hotel.roomName, hotel.boardName].filter(Boolean).join(' · ')}
                  </p>
                )}
              </div>
            </div>

            {/* Booking sidebar */}
            <aside className="booking-card">
              <div className="card">
                <div className="booking-price-header">
                  <div className="price">{formatMoney(hotel.pricePerNight)}</div>
                  <span className="price-sub">/ night</span>
                </div>
                <p className="muted small" style={{ marginBottom: '.75rem' }}>
                  📅 {query.checkIn} → {query.checkOut} · {nights} night{nights === 1 ? '' : 's'}
                </p>

                <PriceBreakdown pricePerNight={hotel.pricePerNight} nights={nights} />

                {!hotel.available && !soldOut && (
                  <p className="field-error" role="alert" style={{ marginTop: '.75rem' }}>
                    This hotel is sold out for your dates. See alternatives below.
                  </p>
                )}

                {!user && hotel.available && !soldOut && (
                  <div className="notice" style={{ marginTop: '.75rem' }}>
                    <Link to="/login" style={{ fontWeight: 700 }}>Sign in</Link> to book and save this trip to your account.
                  </div>
                )}

                {user && hotel.available && !soldOut && (
                  <BookingForm
                    initialGuests={query.guests}
                    initialGuestName={user?.displayName || ''}
                    initialEmail={user?.email || ''}
                    accountUser={user}
                    submitting={submitting}
                    serverError={formError}
                    onSubmit={handleBooking}
                  />
                )}

                {soldOut && (
                  <p className="field-error" role="alert" style={{ marginTop: '.75rem' }}>
                    Those dates just sold out. Pick an alternative below.
                  </p>
                )}
              </div>

              {(soldOut || hotel.alternatives) && (
                <AlternativesPanel
                  alternatives={soldOut || hotel.alternatives}
                  location={hotel.location}
                  hotelId={hotel.id}
                />
              )}
            </aside>
          </div>

          {hotel.available && !soldOut && (
            <div className="booking-bar">
              <div className="booking-bar-price">
                <div className="price">
                  {formatMoney(hotel.pricePerNight)}<span className="price-sub"> / night</span>
                </div>
                <div className="muted small">
                  {formatMoney(Number(hotel.pricePerNight || 0) * Number(nights || 0))} total · {nights} night{nights === 1 ? '' : 's'}
                </div>
              </div>
              {user
                ? <a href="#booking-form" className="btn btn-primary">Book now</a>
                : <Link to="/login" className="btn btn-primary">Sign in to book</Link>}
            </div>
          )}
        </>
      )}
    </section>
  );
}
