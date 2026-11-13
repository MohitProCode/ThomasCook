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
        ? searchHotels({
            location: query.location,
            checkIn: query.checkIn,
            checkOut: query.checkOut,
            guests: Number(query.guests || 2),
          })
        : Promise.resolve({ results: [], total: 0, alternatives: null }),
    [query.location, query.checkIn, query.checkOut],
  );

  const hotels = useMemo(() => {
    let list = [...(data?.results || [])];
    if (availableOnly) {
      list = list.filter((h) => h.available);
    }
    if (sort === 'priceAsc') {
      list.sort((a, b) => a.pricePerNight - b.pricePerNight);
    } else if (sort === 'priceDesc') {
      list.sort((a, b) => b.pricePerNight - a.pricePerNight);
    }
    return list;
  }, [data, sort, availableOnly]);

  if (!paramsValid) {
    return (
      <section>
        <h1>Search results</h1>
        <ErrorBanner message="Those search details look incomplete. Please search again." />
        <p>
          <Link to="/" className="btn btn-secondary">
            Back to search
          </Link>
        </p>
      </section>
    );
  }

  return (
    <section>
      <h1>
        Stays in {query.location}{' '}
        <span className="muted small">
          {query.checkIn} → {query.checkOut} · {query.guests} guest
          {query.guests === 1 ? '' : 's'}
        </span>
      </h1>

      {status === 'loading' && <LoadingSkeleton />}
      {status === 'error' && (
        <ErrorBanner message={error?.message || 'Search failed.'} onRetry={retry} />
      )}

      {status === 'success' && (
        <>
          {data.results.length === 0 && !data.alternatives && (
            <div className="card">
              <p>No hotels found in {query.location}.</p>
              <p>
                <Link to="/" className="btn btn-secondary">
                  Try another destination
                </Link>
              </p>
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
                <label>
                  Sort{' '}
                  <select value={sort} onChange={(e) => setSort(e.target.value)}>
                    <option value="recommended">Recommended</option>
                    <option value="priceAsc">Price: low to high</option>
                    <option value="priceDesc">Price: high to low</option>
                  </select>
                </label>
                <label className="checkbox">
                  <input
                    type="checkbox"
                    checked={availableOnly}
                    onChange={(e) => setAvailableOnly(e.target.checked)}
                  />
                  Available only
                </label>
              </div>
              <div className="results-grid">
                {hotels.map((hotel) => (
                  <HotelCard
                    key={hotel.id}
                    hotel={hotel}
                    checkIn={query.checkIn}
                    checkOut={query.checkOut}
                    guests={query.guests}
                  />
                ))}
              </div>
              {hotels.length === 0 && (
                <p className="muted">No available hotels match the current filters.</p>
              )}
            </>
          )}
        </>
      )}
    </section>
  );
}
