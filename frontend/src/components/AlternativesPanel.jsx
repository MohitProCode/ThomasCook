import { Link } from 'react-router-dom';
import { formatDate } from '../utils/dates.js';
import { formatMoney } from '../utils/money.js';

/**
 * Smart alternatives panel: shown instead of an error when dates are sold out.
 * Never auto-redirects; the traveller picks the next step.
 */
export default function AlternativesPanel({ alternatives, location, hotelId, title }) {
  if (!alternatives) return null;
  const { nearestAvailable, cheapestAlternative } = alternatives;
  if (!nearestAvailable && !cheapestAlternative) return null;

  return (
    <section className="card alternatives" aria-label="Alternative options">
      <h2>{title || 'Sold out for your dates'}</h2>
      <p className="muted">Here are the closest options we found — pick one to continue.</p>

      {nearestAvailable && (
        <div className="alt-block">
          <h3>Nearest available dates</h3>
          <p>
            {formatDate(nearestAvailable.checkIn)} → {formatDate(nearestAvailable.checkOut)}
            {nearestAvailable.nights ? ` · ${nearestAvailable.nights} nights` : ''}
            {nearestAvailable.availableRooms
              ? ` · ${nearestAvailable.availableRooms} room${nearestAvailable.availableRooms === 1 ? '' : 's'}`
              : ''}
          </p>
          {nearestAvailable.message && <p className="muted small">{nearestAvailable.message}</p>}
          <Link
            className="btn btn-secondary"
            to={`/hotels/${hotelId}?checkIn=${nearestAvailable.checkIn}&checkOut=${nearestAvailable.checkOut}`}
          >
            View these dates
          </Link>
        </div>
      )}

      {cheapestAlternative && (
        <div className="alt-block">
          <h3>Cheapest alternative{location ? ` in ${location}` : ''}</h3>
          <p>
            <strong>{cheapestAlternative.name}</strong> ·{' '}
            {formatMoney(cheapestAlternative.pricePerNight)} per night
            {cheapestAlternative.availableRooms
              ? ` · ${cheapestAlternative.availableRooms} room${cheapestAlternative.availableRooms === 1 ? '' : 's'} left`
              : ''}
          </p>
          {cheapestAlternative.message && (
            <p className="muted small">{cheapestAlternative.message}</p>
          )}
          <Link
            className="btn btn-primary"
            to={`/hotels/${cheapestAlternative.hotelId}?checkIn=${cheapestAlternative.checkIn || ''}&checkOut=${cheapestAlternative.checkOut || ''}`}
          >
            View deal
          </Link>
        </div>
      )}
    </section>
  );
}
