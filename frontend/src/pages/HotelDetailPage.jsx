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

  const datesValid = isValidRange(query.checkIn, query.checkOut);
  const nights = nightsBetween(query.checkIn, query.checkOut);

  const { status, data: hotel, error, retry } = useFetch(
    () => getHotel(id, { checkIn: query.checkIn, checkOut: query.checkOut }),
    [id, query.checkIn, query.checkOut],
  );

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
              src={hotel.imageUrl || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1600&q=85'}
              alt={hotel.name}
            />
            <div className="detail-hero-overlay">
              <p className="eyebrow" style={{ color: 'rgba(255,255,255,0.55)', marginBottom: 'var(--space-3)', letterSpacing: '0.12em' }}>
                {hotel.location}
              </p>
              <h1>{hotel.name}</h1>
            </div>
          </div>

          {/* Gallery strip */}
          <div className="detail-gallery-strip">
            {GALLERY_IMGS.map((src, i) => (
              <div className="gallery-thumb" key={i}>
                <img src={src} alt={`${hotel.name} photo ${i + 2}`} loading="lazy" />
              </div>
            ))}
          </div>

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

              {hotel.amenities?.length > 0 && (
                <div className="detail-section">
                  <h2>Amenities</h2>
                  <div className="amenity-grid">
                    {hotel.amenities.map((amenity) => (
                      <div key={amenity} className="amenity-item">
                        <span className="amenity-icon">{AMENITY_ICONS[amenity] || '✓'}</span>
                        {amenity}
                      </div>
                    ))}
                  </div>
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
        </>
      )}
    </section>
  );
}
