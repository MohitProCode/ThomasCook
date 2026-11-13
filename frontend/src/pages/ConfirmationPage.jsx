import { useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { getBooking } from '../api/bookings.js';
import { isEmail } from '../utils/validation.js';
import BookingSummary from '../components/BookingSummary.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function ConfirmationPage() {
  const { reference } = useParams();
  const locationState = useLocation().state;
  const [booking, setBooking] = useState(locationState?.booking || null);
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  async function hydrate(e) {
    e.preventDefault();
    if (!isEmail(email)) { setError('Enter the email used for the booking.'); return; }
    setLoading(true); setError('');
    try { setBooking(await getBooking(reference, email.trim())); }
    catch (err) { setError(err?.message || 'Could not load this booking.'); }
    finally { setLoading(false); }
  }

  async function copyReference() {
    try { await navigator.clipboard.writeText(reference); setCopied(true); setTimeout(() => setCopied(false), 2000); }
    catch { setCopied(false); }
  }

  if (!booking) {
    return (
      <section className="confirmation-page">
        <h1 style={{ marginBottom: '.5rem' }}>Booking confirmation</h1>
        <p className="muted">Enter the booking email to view confirmation for <code>{reference}</code>.</p>
        <div className="card" style={{ marginTop: '1.5rem' }}>
          <form className="form" onSubmit={hydrate}>
            <div className="field">
              <label htmlFor="confirm-email">Booking email</label>
              <input id="confirm-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            {error && <ErrorBanner message={error} />}
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Loading…' : 'View confirmation'}
            </button>
          </form>
        </div>
      </section>
    );
  }

  return (
    <section className="confirmation-page">
      <div className="success-hero animate-scale-in">
        <span className="success-check" aria-hidden="true">✓</span>
        <div>
          <h1 style={{ marginBottom: '.2rem' }}>Booking confirmed!</h1>
          <p className="muted" style={{ margin: 0 }}>Your reservation is secured. Check your email for details.</p>
        </div>
      </div>

      <div className="card animate-fade-up" style={{ animationDelay: '.1s' }}>
        <BookingSummary booking={booking} />
        <div className="confirmation-actions no-print">
          <button type="button" className="btn btn-secondary" onClick={copyReference}>
            {copied ? '✓ Copied!' : '📋 Copy reference'}
          </button>
          <Link to="/my-booking" className="btn btn-secondary">View my booking</Link>
          <Link to="/" className="btn btn-primary">Book another stay</Link>
        </div>
      </div>

      <div className="card notice" style={{ marginTop: '1rem' }}>
        <p style={{ margin: 0, fontSize: '.9rem' }}>
          🎉 <strong>What's next?</strong> A confirmation email has been sent. You can manage your booking anytime from{' '}
          <Link to="/my-booking" style={{ fontWeight: 700 }}>My trips</Link>.
        </p>
      </div>
    </section>
  );
}
