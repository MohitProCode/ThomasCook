import { formatDate } from '../utils/dates.js';
import { formatMoney } from '../utils/money.js';

export default function BookingSummary({ booking }) {
  if (!booking) return null;
  const cancelled = booking.status === 'CANCELLED';

  return (
    <div className="booking-summary">
      <div className="summary-row">
        <span className="muted">Reference</span>
        <code className="reference">{booking.reference}</code>
      </div>
      <div className="summary-row">
        <span className="muted">Status</span>
        <span className={`badge ${cancelled ? 'badge-bad' : 'badge-ok'}`}>
          {cancelled ? '✕ Cancelled' : '✓ Confirmed'}
        </span>
      </div>
      <div className="summary-row">
        <span className="muted">Hotel</span>
        <span style={{ fontWeight: 600 }}>
          {booking.hotelName}
          {booking.location ? <span className="muted"> · {booking.location}</span> : ''}
        </span>
      </div>
      <div className="summary-row">
        <span className="muted">Dates</span>
        <span>
          {formatDate(booking.checkIn)} → {formatDate(booking.checkOut)}
          {booking.nights ? <span className="muted"> ({booking.nights} night{booking.nights === 1 ? '' : 's'})</span> : ''}
        </span>
      </div>
      <div className="summary-row">
        <span className="muted">Guest</span>
        <span>{booking.guestName} · <span className="muted">{booking.email}</span></span>
      </div>
      <div className="summary-row" style={{ paddingTop: '.75rem' }}>
        <span style={{ fontWeight: 700 }}>Total paid</span>
        <strong style={{ fontSize: '1.15rem', color: 'var(--brand)' }}>
          {formatMoney(booking.total, booking.currency)}
        </strong>
      </div>
    </div>
  );
}
