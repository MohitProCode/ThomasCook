import { useState } from 'react';
import { validateBooking } from '../utils/validation.js';

export default function BookingForm({ initialGuests = 2, initialGuestName = '', initialEmail = '', accountUser, submitting, serverError, onSubmit }) {
  const [guestName, setGuestName] = useState(initialGuestName);
  const [email, setEmail] = useState(initialEmail);
  const [guests, setGuests] = useState(initialGuests);
  const [touched, setTouched] = useState(false);

  const errors = validateBooking({ guestName, email });
  const showErrors = touched;

  function handleSubmit(event) {
    event.preventDefault();
    setTouched(true);
    if (Object.keys(errors).length > 0) return;
    onSubmit({ guestName: guestName.trim(), email: email.trim().toLowerCase(), guests });
  }

  return (
    <form className="form" onSubmit={handleSubmit} noValidate>
      {accountUser ? <div className="account-booking-strip"><span className="avatar">{accountUser.displayName.slice(0,1).toUpperCase()}</span><span><strong>Booking as {accountUser.displayName}</strong><small>{accountUser.email}</small></span></div> : <><div className="field">
        <label htmlFor="guestName">Guest name</label>
        <input
          id="guestName"
          type="text"
          autoComplete="name"
          value={guestName}
          onChange={(e) => setGuestName(e.target.value)}
          onBlur={() => setTouched(true)}
          aria-invalid={showErrors && Boolean(errors.guestName)}
          aria-describedby={showErrors && errors.guestName ? 'guestName-error' : undefined}
        />
        {showErrors && errors.guestName && (
          <p className="field-error" id="guestName-error" role="alert">
            {errors.guestName}
          </p>
        )}
      </div>

      <div className="field">
        <label htmlFor="guestEmail">Contact email</label>
        <input
          id="guestEmail"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          onBlur={() => setTouched(true)}
          aria-invalid={showErrors && Boolean(errors.email)}
          aria-describedby={showErrors && errors.email ? 'guestEmail-error' : undefined}
        />
        {showErrors && errors.email && (
          <p className="field-error" id="guestEmail-error" role="alert">
            {errors.email}
          </p>
        )}
      </div></>}

      <div className="field">
        <label htmlFor="guests">Guests</label>
        <select
          id="guests"
          value={guests}
          onChange={(e) => setGuests(Number(e.target.value))}
        >
          {[1, 2, 3, 4, 5, 6].map((n) => (
            <option key={n} value={n}>
              {n} guest{n === 1 ? '' : 's'}
            </option>
          ))}
        </select>
        <p className="muted small">One room per booking in this release.</p>
      </div>

      {serverError && (
        <p className="field-error" role="alert">
          {serverError}
        </p>
      )}

      <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
        {submitting ? 'Confirming…' : 'Confirm booking'}
      </button>
    </form>
  );
}
