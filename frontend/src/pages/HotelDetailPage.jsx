import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getHotel } from '../api/hotels.js';
import { createBooking } from '../api/bookings.js';
import { ApiError } from '../api/client.js';
import { useQueryParams } from '../hooks/useQueryParams.js';
import { useFetch } from '../hooks/useFetch.js';
import { useBooking } from '../context/BookingContext.jsx';
import { isValidRange, nightsBetween } from '../utils/dates.js';
import { AvailabilityBadge } from '../components/HotelCard.jsx';
import AlternativesPanel from '../components/AlternativesPanel.jsx';
import PriceBreakdown from '../components/PriceBreakdown.jsx';
import BookingForm from '../components/BookingForm.jsx';
import LoadingSkeleton from '../components/LoadingSkeleton.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';
import { useAuth } from '../context/AuthContext.jsx';

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
        <h1>Hotel details</h1>
        <ErrorBanner message="Pick valid dates to see availability and book." />
        <p>
          <Link to="/" className="btn btn-secondary">
            Back to search
          </Link>
        </p>
      </section>
    );
  }

  return (
    <section>
      {status === 'loading' && <LoadingSkeleton rows={2} />}
      {status === 'error' && (
        <>
          <ErrorBanner message={error?.message || 'Could not load this hotel.'} onRetry={retry} />
          <p>
            <Link to="/results" className="btn btn-secondary">
              Back to results
            </Link>
          </p>
        </>
      )}

      {status === 'success' && hotel && (
        <div className="detail-layout">
          <div>
            <p>
              <Link to={`/results?location=${encodeURIComponent(hotel.location)}&checkIn=${query.checkIn}&checkOut=${query.checkOut}&guests=${query.guests}`}>
                ← Back to results
              </Link>
            </p>
            <h1>{hotel.name}</h1>
            <p className="muted">{hotel.location}</p>
            <p>
              <AvailabilityBadge hotel={hotel} />
            </p>
            <p>{hotel.description}</p>
            {hotel.amenities?.length > 0 && (
              <>
                <h2>Amenities</h2>
                <ul className="chips">
                  {hotel.amenities.map((amenity) => (
                    <li key={amenity} className="chip">
                      {amenity}
                    </li>
                  ))}
                </ul>
              </>
            )}
            <div className="availability-panel card">
              <p className="eyebrow">LIVE INVENTORY</p>
              <h2>Availability for your stay</h2>
              <p className="muted">
                Rates and room availability are checked for the selected check-in and check-out dates.
                Nuitee does not guarantee a future day-by-day room calendar on standard accounts.
              </p>
            </div>
          </div>

          <aside className="booking-card">
            <div className="card">
              <h2>Your stay</h2>
              <p className="muted small">
                {query.checkIn} → {query.checkOut} · {nights} night{nights === 1 ? '' : 's'}
              </p>
              <PriceBreakdown pricePerNight={hotel.pricePerNight} nights={nights} />

              {!hotel.available && !soldOut && (
                <p className="field-error" role="alert">
                  This hotel is sold out for your dates. Booking is disabled — see alternatives
                  below.
                </p>
              )}

              {!user && hotel.available && !soldOut && (
                <p className="notice">Please <a href="/login">sign in</a> before booking so we can save this trip to your account.</p>
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
                <p className="field-error" role="alert">
                  Those dates just sold out. Pick an alternative below instead.
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
      )}
    </section>
  );
}
