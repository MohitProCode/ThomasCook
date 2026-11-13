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

  async function hydrate(event) {
    event.preventDefault();
    if (!isEmail(email)) {
      setError('Enter the email used for the booking.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      setBooking(await getBooking(reference, email.trim()));
    } catch (err) {
      setError(err?.message || 'Could not load this booking.');
    } finally {
      setLoading(false);
    }
  }

  async function copyReference() {
    try {
      await navigator.clipboard.writeText(reference);
      setCopied(true);
    } catch {
      setCopied(false);
    }
  }

  if (!booking) {
    return (
      <section>
        <h1>Booking confirmation</h1>
        <p className="muted">
          You opened this page directly. Enter the booking email to view confirmation{' '}
          <code>{reference}</code>.
        </p>
        <form className="card form" onSubmit={hydrate}>
          <div className="field">
            <label htmlFor="confirm-email">Booking email</label>
            <input
              id="confirm-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          {error && <ErrorBanner message={error} />}
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Loading…' : 'View confirmation'}
          </button>
        </form>
      </section>
    );
  }

  return (
    <section>
      <div className="success-hero">
        <span className="success-check" aria-hidden="true">
          ✓
        </span>
        <h1>Booking confirmed</h1>
      </div>
      <div className="card">
        <BookingSummary booking={booking} />
        <div className="confirmation-actions no-print">
          <button type="button" className="btn btn-secondary" onClick={copyReference}>
            {copied ? 'Copied!' : 'Copy reference'}
          </button>
          <Link to="/my-booking" className="btn btn-secondary">
            View my booking
          </Link>
          <Link to="/" className="btn btn-primary">
            Book another stay
          </Link>
        </div>
      </div>
    </section>
  );
}
