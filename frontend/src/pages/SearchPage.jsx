import { lazy, Suspense, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { gsap } from 'gsap';
import { Link, useNavigate } from 'react-router-dom';
import { MapPin, CalendarBlank, Users, MagnifyingGlass, CheckCircle } from '@phosphor-icons/react';
import { useBooking } from '../context/BookingContext.jsx';
import { validateSearch } from '../utils/validation.js';
import { todayISO } from '../utils/dates.js';
import { searchHotels } from '../api/hotels.js';

const HotelGlobe = lazy(() => import('../components/HotelGlobe.jsx'));

const DESTINATIONS = [
  { name: 'Goa', label: 'Beach paradise', img: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=800&q=80' },
  { name: 'Jaipur', label: 'Pink City', img: 'https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=800&q=80' },
  { name: 'Manali', label: 'Mountain escape', img: 'https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=800&q=80' },
  { name: 'Mumbai', label: 'City of dreams', img: 'https://images.unsplash.com/photo-1570168007204-dfb528c6958f?auto=format&fit=crop&w=800&q=80' },
  { name: 'Kerala', label: 'God\'s own country', img: 'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=800&q=80' },
];

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
    const ctx = gsap.context(() => {
      gsap.from('.hero-copy > *', { y: 28, opacity: 0, duration: 0.75, stagger: 0.1, ease: 'power3.out' });
      gsap.from('.search-float', { y: 32, opacity: 0, scale: 0.97, duration: 0.8, delay: 0.2, ease: 'power3.out' });
      gsap.from('.trust-row span', { y: 12, opacity: 0, duration: 0.45, delay: 0.45, stagger: 0.08, ease: 'power2.out' });
    }, pageRef);
    return () => ctx.revert();
  }, []);

  const errors = validateSearch({ location, checkIn, checkOut, guests });

  function handleSubmit(e) {
    e.preventDefault();
    setTouched(true);
    if (Object.keys(errors).length > 0) return;
    const search = { location: location.trim(), checkIn, checkOut, guests };
    saveSearch(search);
    navigate(`/results?${new URLSearchParams(search).toString()}`);
  }

  function quickSearch(dest) {
    const today = todayISO();
    const ci = today > '2026-12-19' ? today : '2026-12-20';
    const co = '2026-12-23';
    const search = { location: dest, checkIn: ci, checkOut: co, guests: 2 };
    saveSearch(search);
    navigate(`/results?${new URLSearchParams(search).toString()}`);
  }

  return (
    <div className="home-page" ref={pageRef}>
      {/* ── Hero ── */}
      <section className="hero-section">
        <div className="hero-bg" />
        <div className="hero-copy">
          <p className="eyebrow" style={{ color: 'rgba(255,255,255,0.6)', marginBottom: 'var(--space-3)' }}>TRAVEL YOUR WAY</p>
          <h1>Make every stay<br /><em>extraordinary</em></h1>
          <p>From quick city breaks to slow beach mornings — find a place that feels right.</p>
          <div className="trust-row" style={{ marginTop: '1.5rem' }}>
            <span><CheckCircle size={14} weight="fill" aria-hidden="true" /> Best price guaranteed</span>
            <span><CheckCircle size={14} weight="fill" aria-hidden="true" /> Free cancellation</span>
            <span><CheckCircle size={14} weight="fill" aria-hidden="true" /> 24/7 support</span>
          </div>
        </div>
      </section>

      {/* ── Floating search bar ── */}
      <div className="search-float">
        <form className="search-form" onSubmit={handleSubmit} noValidate>
          <div className="search-field search-destination">
            <div className="field-icon-wrap"><MapPin size={18} weight="regular" aria-hidden="true" /></div>
            <div className="field">
              <label htmlFor="location">Where are you going?</label>
              <input
                id="location"
                type="text"
                placeholder="Goa, Jaipur, Manali…"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                aria-invalid={touched && Boolean(errors.location)}
              />
            </div>
          </div>

          <div className="search-field">
            <div className="field-icon-wrap"><CalendarBlank size={18} weight="regular" aria-hidden="true" /></div>
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
            </div>
          </div>

          <div className="search-field">
            <div className="field-icon-wrap"><CalendarBlank size={18} weight="regular" aria-hidden="true" /></div>
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
            </div>
          </div>

          <div className="search-field">
            <div className="field-icon-wrap"><Users size={18} weight="regular" aria-hidden="true" /></div>
            <div className="field">
              <label htmlFor="guests">Guests</label>
              <select id="guests" value={guests} onChange={(e) => setGuests(Number(e.target.value))}>
                {[1, 2, 3, 4, 5, 6].map((n) => (
                  <option key={n} value={n}>{n} guest{n === 1 ? '' : 's'}</option>
                ))}
              </select>
            </div>
          </div>

          <button type="submit" className="btn btn-primary search-submit">
            <MagnifyingGlass size={16} weight="bold" aria-hidden="true" /> Search
          </button>

          {touched && Object.keys(errors).length > 0 && (
            <p className="form-error" role="alert">Please fill in all required fields.</p>
          )}
        </form>
      </div>

      {/* ── Recent search ── */}
      {lastSearch?.location && (
        <div className="recent-search">
          <span className="recent-label">Recent:</span>
          <Link
            to={`/results?${new URLSearchParams(lastSearch).toString()}`}
            className="recent-chip"
          >
            {lastSearch.location} · {lastSearch.checkIn} → {lastSearch.checkOut}
          </Link>
        </div>
      )}

      {/* ── Popular destinations ── */}
      <section className="destinations-section">
        <div className="section-header">
          <h2>Popular destinations</h2>
          <Link to="/results?location=&checkIn=2026-12-20&checkOut=2026-12-23&guests=2">View all →</Link>
        </div>
        <div className="destinations-grid">
          {DESTINATIONS.map((dest) => (
            <button
              key={dest.name}
              className="dest-card"
              type="button"
              onClick={() => quickSearch(dest.name)}
              style={{ border: 'none', padding: 0, background: 'none', cursor: 'pointer' }}
            >
              <img className="dest-img" src={dest.img} alt={dest.name} loading="lazy" />
              <div className="dest-overlay">
                <p className="dest-name">{dest.name}</p>
                <p className="dest-count">{dest.label}</p>
              </div>
            </button>
          ))}
        </div>
      </section>

      {/* ── Globe ── */}
      <section className="globe-section" aria-labelledby="globe-title">
        <div className="globe-copy">
          <p className="eyebrow">EXPLORE THE MAP</p>
          <h2 id="globe-title">Stay somewhere<br /><span>worth remembering.</span></h2>
          <p>Browse hotel regions at a glance, then use the search above to lock in your dates.</p>
          <div className="globe-stats">
            <strong>{mapTotal || '—'}</strong>
            <span>hotel stays indexed</span>
          </div>
          <div className="globe-regions">
            {[...new Set(mapHotels.map((h) => h.location).filter(Boolean))].slice(0, 4).map((region) => (
              <span key={region}><i />{region}</span>
            ))}
          </div>
          <p className="globe-hint">Drag the globe to explore</p>
        </div>
        <Suspense fallback={<div className="hotel-globe" style={{ display: 'grid', placeItems: 'center', color: '#78e2d3' }}>Loading…</div>}>
          <HotelGlobe hotels={mapHotels} />
        </Suspense>
      </section>
    </div>
  );
}
