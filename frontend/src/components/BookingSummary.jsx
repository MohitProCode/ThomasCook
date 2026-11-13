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
        <span className={`badge ${cancelled ? '' : 'badge-ok'}`}>{booking.status}</span>
      </div>
      <div className="summary-row">
        <span className="muted">Hotel</span>
        <span>
          {booking.hotelName}
          {booking.location ? ` · ${booking.location}` : ''}
        </span>
      </div>
      <div className="summary-row">
        <span className="muted">Dates</span>
        <span>
          {formatDate(booking.checkIn)} → {formatDate(booking.checkOut)}
          {booking.nights ? ` (${booking.nights} night${booking.nights === 1 ? '' : 's'})` : ''}
        </span>
      </div>
      <div className="summary-row">
        <span className="muted">Guest</span>
        <span>
          {booking.guestName} · {booking.email}
        </span>
      </div>
      <div className="summary-row">
        <span className="muted">Total</span>
        <strong>{formatMoney(booking.total, booking.currency)}</strong>
      </div>
    </div>
  );
}
