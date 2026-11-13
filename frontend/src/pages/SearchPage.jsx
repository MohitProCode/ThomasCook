import { lazy, Suspense, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { gsap } from 'gsap';
import { Link, useNavigate } from 'react-router-dom';
import { useBooking } from '../context/BookingContext.jsx';
import { validateSearch } from '../utils/validation.js';
import { todayISO } from '../utils/dates.js';
import { searchHotels } from '../api/hotels.js';

const HotelGlobe = lazy(() => import('../components/HotelGlobe.jsx'));

export default function SearchPage() {
  const navigate = useNavigate();
  const { lastSearch, saveSearch } = useBooking();
  const [location, setLocation] = useState(lastSearch?.location || '');
  const [checkIn, setCheckIn] = useState(lastSearch?.checkIn || '');
  const [checkOut, setCheckOut] = useState(lastSearch?.checkOut || '');
  const [guests, setGuests] = useState(lastSearch?.guests || 2);
  const [touched, setTouched] = useState(false);
  const [mapHotels, setMapHotels] = useState([]);
  const [mapTotal, setMapTotal] = useState(0);
  const pageRef = useRef(null);

  useEffect(() => {
    let active = true;
    searchHotels({ location: '', checkIn: '', checkOut: '' })
      .then((data) => { if (active) { setMapHotels(data.results || []); setMapTotal(data.total || data.results?.length || 0); } })
      .catch(() => { if (active) setMapHotels([]); });
    return () => { active = false; };
  }, []);

  useLayoutEffect(() => {
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduceMotion || !pageRef.current) return undefined;
    const context = gsap.context(() => {
      gsap.from('.hero-copy > *', { y: 24, opacity: 0, duration: 0.7, stagger: 0.08, ease: 'power3.out' });
      gsap.from('.search-form', { y: 26, opacity: 0, scale: 0.98, duration: 0.75, delay: 0.18, ease: 'power3.out' });
      gsap.from('.trust-row span', { y: 12, opacity: 0, duration: 0.45, delay: 0.42, stagger: 0.08, ease: 'power2.out' });
    }, pageRef);
    return () => context.revert();
  }, []);

  const errors = validateSearch({ location, checkIn, checkOut, guests });

  function handleSubmit(event) {
    event.preventDefault();
    setTouched(true);
    if (Object.keys(errors).length > 0) return;
    const search = { location: location.trim(), checkIn, checkOut, guests };
    saveSearch(search);
    const params = new URLSearchParams(search);
    navigate(`/results?${params.toString()}`);
  }

  return (
    <section className="home-page" ref={pageRef}>
      <div className="hero-copy">
        <p className="eyebrow">TRAVEL YOUR WAY</p>
        <h1>Make every stay<br /><span>extraordinary</span></h1>
        <p>From quick city breaks to slow beach mornings, find a place that feels right.</p>
      </div>

      <form className="card form search-form" onSubmit={handleSubmit} noValidate>
        <div className="search-field search-destination">
          <span className="field-icon location-icon" aria-hidden="true" />
          <div className="field">
            <label htmlFor="location">Where are you going?</label>
          <input
            id="location"
            type="text"
            placeholder="e.g. Goa, Jaipur, Manali"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            aria-invalid={touched && Boolean(errors.location)}
          />
          {touched && errors.location && (
            <p className="field-error" role="alert">
              {errors.location}
            </p>
          )}
          </div>
        </div>

        <div className="form-row">
          <div className="search-field">
            <span className="field-icon date-icon" aria-hidden="true" />
            <div className="field">
            <label htmlFor="checkIn">Check-in</label>
            <input
              id="checkIn"
              type="date"
              min={todayISO()}
              value={checkIn}
              onChange={(e) => setCheckIn(e.target.value)}
              aria-invalid={touched && Boolean(errors.checkIn)}
            />
            {touched && errors.checkIn && (
              <p className="field-error" role="alert">
                {errors.checkIn}
              </p>
            )}
            </div>
          </div>
          <div className="search-field">
            <span className="field-icon date-icon" aria-hidden="true" />
            <div className="field">
            <label htmlFor="checkOut">Check-out</label>
            <input
              id="checkOut"
              type="date"
              min={checkIn || todayISO()}
              value={checkOut}
              onChange={(e) => setCheckOut(e.target.value)}
              aria-invalid={touched && Boolean(errors.checkOut)}
            />
            {touched && errors.checkOut && (
              <p className="field-error" role="alert">
                {errors.checkOut}
              </p>
            )}
            </div>
          </div>
          <div className="search-field">
            <span className="field-icon guest-icon" aria-hidden="true" />
            <div className="field">
            <label htmlFor="guests">Guests</label>
            <select id="guests" value={guests} onChange={(e) => setGuests(Number(e.target.value))}>
              {[1, 2, 3, 4, 5, 6].map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
            </div>
          </div>
        </div>

        <button type="submit" className="btn btn-primary search-submit">
          Search hotels
        </button>

        {touched && Object.keys(errors).length > 0 && (
          <p className="field-error form-error" role="alert">
            Please fix the highlighted fields.
          </p>
        )}
      </form>

      <div className="trust-row">
        <span><b>✓</b> Best price guaranteed</span>
        <span><b>✓</b> Free cancellation on most stays</span>
        <span><b>✓</b> 24/7 customer support</span>
      </div>

      <section className="globe-section" aria-labelledby="globe-title">
        <div className="globe-copy">
          <p className="eyebrow">EXPLORE THE MAP</p>
          <h2 id="globe-title">Stay somewhere<br /><span>worth remembering.</span></h2>
          <p>Browse hotel regions at a glance, then use the search above to lock in your dates and stay.</p>
          <div className="globe-stats"><strong>{mapTotal || '—'}</strong><span>hotel stays indexed</span></div>
          <div className="globe-regions">
            {[...new Set(mapHotels.map((hotel) => hotel.location).filter(Boolean))].slice(0, 4).map((region) => (
              <span key={region}><i />{region}</span>
            ))}
          </div>
          <p className="globe-hint">Drag the globe to explore</p>
        </div>
        <Suspense fallback={<div className="hotel-globe globe-loading">Loading regions…</div>}>
          <HotelGlobe hotels={mapHotels} />
        </Suspense>
      </section>

      {lastSearch?.location && (
        <p className="recent-search">
          Recent search:{' '}
          <Link
            to={`/results?${new URLSearchParams(lastSearch).toString()}`}
            className="recent-chip"
          >
            {lastSearch.location} · {lastSearch.checkIn} → {lastSearch.checkOut}
          </Link>
        </p>
      )}
    </section>
  );
}
