import { Link } from 'react-router-dom';
import { Lightbulb, CalendarBlank, CurrencyDollar } from '@phosphor-icons/react';
import { formatDate } from '../utils/dates.js';
import { formatMoney } from '../utils/money.js';

export default function AlternativesPanel({ alternatives, location, hotelId, title }) {
  if (!alternatives) return null;
  const { nearestAvailable, cheapestAlternative } = alternatives;
  if (!nearestAvailable && !cheapestAlternative) return null;

  return (
    <section className="card alternatives animate-fade-up" aria-label="Alternative options">
      <div style={{ display: 'flex', alignItems: 'center', gap: '.6rem', marginBottom: '.5rem' }}>
        <Lightbulb size={22} weight="fill" style={{ color: 'var(--accent)', flexShrink: 0 }} aria-hidden="true" />
        <h2 style={{ margin: 0, fontSize: '1.1rem' }}>{title || 'Sold out for your dates'}</h2>
      </div>
      <p className="muted" style={{ fontSize: '.88rem', marginBottom: '1rem' }}>
        Here are the closest options we found — pick one to continue.
      </p>

      {nearestAvailable && (
        <div className="alt-block">
          <p style={{ fontSize: '.72rem', fontWeight: 700, color: 'var(--brand)', textTransform: 'uppercase', letterSpacing: '.08em', margin: '0 0 .4rem', display: 'flex', alignItems: 'center', gap: '.3rem' }}>
            <CalendarBlank size={12} weight="bold" aria-hidden="true" /> Nearest available dates
          </p>
          <p style={{ margin: '0 0 .5rem', fontWeight: 600 }}>
            {formatDate(nearestAvailable.checkIn)} → {formatDate(nearestAvailable.checkOut)}
            {nearestAvailable.nights ? ` · ${nearestAvailable.nights} nights` : ''}
            {nearestAvailable.availableRooms ? ` · ${nearestAvailable.availableRooms} room${nearestAvailable.availableRooms === 1 ? '' : 's'} left` : ''}
          </p>
          {nearestAvailable.message && <p className="muted small">{nearestAvailable.message}</p>}
          <Link className="btn btn-secondary btn-small" to={`/hotels/${hotelId}?checkIn=${nearestAvailable.checkIn}&checkOut=${nearestAvailable.checkOut}`}>
            View these dates →
          </Link>
        </div>
      )}

      {cheapestAlternative && (
        <div className="alt-block">
          <p style={{ fontSize: '.72rem', fontWeight: 700, color: 'var(--ok)', textTransform: 'uppercase', letterSpacing: '.08em', margin: '0 0 .4rem', display: 'flex', alignItems: 'center', gap: '.3rem' }}>
            <CurrencyDollar size={12} weight="bold" aria-hidden="true" /> Cheapest alternative{location ? ` in ${location}` : ''}
          </p>
          <p style={{ margin: '0 0 .25rem', fontWeight: 700 }}>{cheapestAlternative.name}</p>
          <p className="muted small" style={{ margin: '0 0 .6rem' }}>
            {formatMoney(cheapestAlternative.pricePerNight)} per night
            {cheapestAlternative.availableRooms ? ` · ${cheapestAlternative.availableRooms} room${cheapestAlternative.availableRooms === 1 ? '' : 's'} left` : ''}
          </p>
          {cheapestAlternative.message && <p className="muted small">{cheapestAlternative.message}</p>}
          <Link
            className="btn btn-primary btn-small"
            to={`/hotels/${cheapestAlternative.hotelId}?checkIn=${cheapestAlternative.checkIn || ''}&checkOut=${cheapestAlternative.checkOut || ''}`}
          >
            View deal →
          </Link>
        </div>
      )}
    </section>
  );
}
