import { useEffect, useState } from 'react';
import { cancelBooking, getBooking, getMyBookings } from '../api/bookings.js';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { validateLookup } from '../utils/validation.js';
import BookingSummary from '../components/BookingSummary.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function MyBookingPage() {
  const { user } = useAuth();
  const [reference, setReference] = useState('');
  const [email, setEmail] = useState('');
  const [touched, setTouched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [booking, setBooking] = useState(null);
  const [cancelling, setCancelling] = useState(false);
  const [cancelArmed, setCancelArmed] = useState(false);
  const [notice, setNotice] = useState('');
  const [myBookings, setMyBookings] = useState([]);

  useEffect(() => { if (user) getMyBookings().then(setMyBookings).catch(() => {}); }, [user]);

  const errors = validateLookup({ reference, email });

  async function handleLookup(event) {
    event.preventDefault();
    setTouched(true);
    setNotice('');
    if (Object.keys(errors).length > 0) return;
    setLoading(true);
    setError('');
    setBooking(null);
    setCancelArmed(false);
    try {
      setBooking(await getBooking(reference.trim().toUpperCase(), email.trim()));
    } catch (err) {
      setError(err?.message || 'Lookup failed.');
    } finally {
      setLoading(false);
    }
  }

  async function handleCancel() {
    if (!cancelArmed) {
      setCancelArmed(true);
      return;
    }
    setCancelling(true);
    setError('');
    try {
      await cancelBooking(booking.reference, email.trim());
      setBooking({ ...booking, status: 'CANCELLED' });
      setNotice('Booking cancelled. Availability has been released.');
      setCancelArmed(false);
    } catch (err) {
      setError(err?.message || 'Cancel failed.');
    } finally {
      setCancelling(false);
    }
  }

  const cancelled = booking?.status === 'CANCELLED';

  return (
    <section>
      <h1>My booking</h1>
      <p className="muted">{user ? `Signed in as ${user.email}.` : 'Sign in to view and manage your bookings.'}</p>

      {!user && <p><Link className="btn btn-primary" to="/login">Sign in</Link></p>}

      {user && myBookings.length > 0 && <div className="results-grid">{myBookings.map(item => <button key={item.reference} className="card" type="button" onClick={() => setBooking(item)}><strong>{item.hotelName}</strong><br/><span className="muted">{item.reference} · {item.checkIn} → {item.checkOut}</span></button>)}</div>}

      <form className="card form" onSubmit={handleLookup} noValidate>
        <div className="form-row">
          <div className="field">
            <label htmlFor="reference">Booking reference</label>
            <input
              id="reference"
              type="text"
              placeholder="TC-XXXXXX"
              value={reference}
              onChange={(e) => setReference(e.target.value.toUpperCase())}
              aria-invalid={touched && Boolean(errors.reference)}
            />
            {touched && errors.reference && (
              <p className="field-error" role="alert">
                {errors.reference}
              </p>
            )}
          </div>
          <div className="field">
            <label htmlFor="lookup-email">Email</label>
            <input
              id="lookup-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              aria-invalid={touched && Boolean(errors.email)}
            />
            {touched && errors.email && (
              <p className="field-error" role="alert">
                {errors.email}
              </p>
            )}
          </div>
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Looking up…' : 'Find booking'}
        </button>
      </form>

      {error && <ErrorBanner message={error} onDismiss={() => setError('')} />}
      {notice && (
        <p className="notice" role="status">
          {notice}
        </p>
      )}

      {booking && (
        <div className="card">
          <BookingSummary booking={booking} />
          {!cancelled && (
            <div className="cancel-zone">
              <button
                type="button"
                className="btn btn-danger"
                onClick={handleCancel}
                disabled={cancelling}
              >
                {cancelling
                  ? 'Cancelling…'
                  : cancelArmed
                    ? 'Click again to confirm cancel'
                    : 'Cancel booking'}
              </button>
              {cancelArmed && !cancelling && (
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => setCancelArmed(false)}
                >
                  Keep booking
                </button>
              )}
            </div>
          )}
        </div>
      )}
    </section>
  );
}
