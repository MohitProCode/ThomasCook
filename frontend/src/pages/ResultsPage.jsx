import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { searchHotels } from '../api/hotels.js';
import { useQueryParams } from '../hooks/useQueryParams.js';
import { useFetch } from '../hooks/useFetch.js';
import { validateSearch } from '../utils/validation.js';
import HotelCard from '../components/HotelCard.jsx';
import AlternativesPanel from '../components/AlternativesPanel.jsx';
import LoadingSkeleton from '../components/LoadingSkeleton.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function ResultsPage() {
  const query = useQueryParams();
  const [sort, setSort] = useState('recommended');
  const [availableOnly, setAvailableOnly] = useState(false);

  const paramErrors = validateSearch(query);
  const paramsValid = Object.keys(paramErrors).length === 0;

  const { status, data, error, retry } = useFetch(
    () =>
      paramsValid
        ? searchHotels({ location: query.location, checkIn: query.checkIn, checkOut: query.checkOut, guests: Number(query.guests || 2) })
        : Promise.resolve({ results: [], total: 0, alternatives: null }),
    [query.location, query.checkIn, query.checkOut],
  );

  const hotels = useMemo(() => {
    let list = [...(data?.results || [])];
    if (availableOnly) list = list.filter((h) => h.available);
    if (sort === 'priceAsc') list.sort((a, b) => a.pricePerNight - b.pricePerNight);
    else if (sort === 'priceDesc') list.sort((a, b) => b.pricePerNight - a.pricePerNight);
    return list;
  }, [data, sort, availableOnly]);

  if (!paramsValid) {
    return (
      <section>
        <ErrorBanner message="Those search details look incomplete. Please search again." />
        <p><Link to="/" className="btn btn-secondary">← Back to search</Link></p>
      </section>
    );
  }

  const nights = query.checkIn && query.checkOut
    ? Math.round((new Date(query.checkOut) - new Date(query.checkIn)) / 86400000)
    : null;

  return (
    <section>
      <div className="results-header">
        <h1>Stays in {query.location}</h1>
        <p className="results-meta">
          {query.checkIn} → {query.checkOut}
          {nights ? ` · ${nights} night${nights === 1 ? '' : 's'}` : ''}
          {' · '}{query.guests || 2} guest{Number(query.guests) === 1 ? '' : 's'}
          {status === 'success' && data?.results?.length > 0 && (
            <> · <strong>{data.results.length} hotel{data.results.length === 1 ? '' : 's'} found</strong></>
          )}
        </p>
      </div>

      {status === 'loading' && <LoadingSkeleton rows={6} />}
      {status === 'error' && <ErrorBanner message={error?.message || 'Search failed.'} onRetry={retry} />}

      {status === 'success' && (
        <>
          {data.results.length === 0 && !data.alternatives && (
            <div className="card" style={{ textAlign: 'center', padding: '3rem 2rem' }}>
              <p style={{ fontSize: '3rem', margin: '0 0 1rem' }}>🏨</p>
              <h2>No hotels found in {query.location}</h2>
              <p className="muted">Try a different destination or adjust your dates.</p>
              <Link to="/" className="btn btn-primary" style={{ marginTop: '1rem' }}>Search again</Link>
            </div>
          )}

          {data.results.length === 0 && data.alternatives && (
            <AlternativesPanel
              alternatives={data.alternatives}
              location={query.location}
              title={`Sold out in ${query.location} for your dates`}
            />
          )}

          {data.results.length > 0 && (
            <>
              <div className="results-toolbar no-print">
                <span className="toolbar-label">Sort by</span>
                <select value={sort} onChange={(e) => setSort(e.target.value)}>
                  <option value="recommended">Recommended</option>
                  <option value="priceAsc">Price: low → high</option>
                  <option value="priceDesc">Price: high → low</option>
                </select>

                <label className="checkbox">
                  <input
                    type="checkbox"
                    checked={availableOnly}
                    onChange={(e) => setAvailableOnly(e.target.checked)}
                  />
                  Available only
                </label>

                <span className="results-count">
                  {hotels.length} of {data.results.length} shown
                </span>
              </div>

              <div className="results-grid">
                {hotels.map((hotel, i) => (
                  <HotelCard
                    key={hotel.id}
                    hotel={hotel}
                    checkIn={query.checkIn}
                    checkOut={query.checkOut}
                    guests={query.guests}
                    style={{ animationDelay: `${i * 0.06}s` }}
                  />
                ))}
              </div>

              {hotels.length === 0 && (
                <p className="muted" style={{ textAlign: 'center', padding: '2rem' }}>
                  No available hotels match the current filters.
                </p>
              )}
            </>
          )}
        </>
      )}
    </section>
  );
}
