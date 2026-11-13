import { useEffect, useState } from 'react';
import { cancelBooking, getBooking, getMyBookings } from '../api/bookings.js';
import { Link } from 'react-router-dom';
import { MagnifyingGlass, CalendarBlank } from '@phosphor-icons/react';
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

  useEffect(() => {
    if (user) getMyBookings().then(setMyBookings).catch(() => {});
  }, [user]);

  const errors = validateLookup({ reference, email });

  async function handleLookup(e) {
    e.preventDefault();
    setTouched(true); setNotice('');
    if (Object.keys(errors).length > 0) return;
    setLoading(true); setError(''); setBooking(null); setCancelArmed(false);
    try { setBooking(await getBooking(reference.trim().toUpperCase(), email.trim())); }
    catch (err) { setError(err?.message || 'Lookup failed.'); }
    finally { setLoading(false); }
  }

  async function handleCancel() {
    if (!cancelArmed) { setCancelArmed(true); return; }
    setCancelling(true); setError('');
    try {
      await cancelBooking(booking.reference, email.trim());
      setBooking({ ...booking, status: 'CANCELLED' });
      setNotice('Booking cancelled. Availability has been released.');
      setCancelArmed(false);
    } catch (err) { setError(err?.message || 'Cancel failed.'); }
    finally { setCancelling(false); }
  }

  const cancelled = booking?.status === 'CANCELLED';

  return (
    <section style={{ maxWidth: 720, margin: '0 auto' }}>
      <div style={{ padding: '2rem 0 1rem' }}>
        <h1 className="page-title">My trips</h1>
        <p className="muted">
          {user ? `Signed in as ${user.email}.` : 'Sign in to view and manage your bookings, or look up by reference below.'}
        </p>
        {!user && <Link className="btn btn-primary" to="/login" style={{ marginTop: '.5rem' }}>Sign in</Link>}
      </div>

      {user && myBookings.length > 0 && (
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.1rem', marginBottom: '.85rem' }}>Your bookings</h2>
          <div style={{ display: 'grid', gap: '.75rem' }}>
            {myBookings.map((item) => (
              <button
                key={item.reference}
                className="card"
                type="button"
                onClick={() => setBooking(item)}
                style={{ textAlign: 'left', cursor: 'pointer', border: booking?.reference === item.reference ? '2px solid var(--brand)' : '1px solid var(--border)', transition: 'border-color var(--t-fast), box-shadow var(--t-fast)' }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                  <div>
                    <strong style={{ fontSize: '1rem' }}>{item.hotelName}</strong>
                    <p className="muted small" style={{ margin: '.2rem 0 0' }}>
                      <CalendarBlank size={12} weight="regular" aria-hidden="true" style={{ verticalAlign: 'middle', marginRight: 3 }} />
                      {item.checkIn} → {item.checkOut}
                    </p>
                  </div>
                  <code style={{ fontSize: '.82rem', color: 'var(--brand)', fontWeight: 700 }}>{item.reference}</code>
                </div>
              </button>
            ))}
          </div>
        </div>
      )}

      <div className="card">
        <h2 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Look up a booking</h2>
        <form className="form" onSubmit={handleLookup} noValidate>
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
              {touched && errors.reference && <p className="field-error" role="alert">{errors.reference}</p>}
            </div>
            <div className="field">
              <label htmlFor="lookup-email">Email address</label>
              <input
                id="lookup-email"
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                aria-invalid={touched && Boolean(errors.email)}
              />
              {touched && errors.email && <p className="field-error" role="alert">{errors.email}</p>}
            </div>
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading} style={{ justifySelf: 'start' }}>
            {loading ? 'Looking up…' : <><MagnifyingGlass size={15} weight="bold" aria-hidden="true" /> Find booking</>}
          </button>
        </form>
      </div>

      {error && <ErrorBanner message={error} onDismiss={() => setError('')} />}
      {notice && <p className="notice" role="status">{notice}</p>}

      {booking && (
        <div className="card animate-fade-up" style={{ marginTop: '1rem' }}>
          <BookingSummary booking={booking} />
          {!cancelled && (
            <div className="cancel-zone">
              <button
                type="button"
                className={`btn ${cancelArmed ? 'btn-danger' : 'btn-secondary'}`}
                onClick={handleCancel}
                disabled={cancelling}
              >
                {cancelling ? 'Cancelling…' : cancelArmed ? '⚠️ Confirm cancellation' : 'Cancel booking'}
              </button>
              {cancelArmed && !cancelling && (
                <button type="button" className="btn btn-ghost" onClick={() => setCancelArmed(false)}>
                  Keep booking
                </button>
              )}
            </div>
          )}
          {cancelled && (
            <p className="muted small" style={{ marginTop: '.75rem' }}>
              This booking has been cancelled.
            </p>
          )}
        </div>
      )}
    </section>
  );
}
