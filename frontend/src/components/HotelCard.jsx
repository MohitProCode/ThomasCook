import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Heart, Star, MapPin } from '@phosphor-icons/react';
import { formatMoney } from '../utils/money.js';

const FALLBACK_IMAGES = [
  'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=75',
  'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=75',
  'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=800&q=75',
  'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=75',
  'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=800&q=75',
  'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=800&q=75',
];

function StarRating({ rating = 4.2 }) {
  const full = Math.floor(rating);
  return (
    <span className="stars" aria-label={`${rating} out of 5`}>
      {Array.from({ length: 5 }, (_, i) => (
        <Star
          key={i}
          size={12}
          weight={i < full ? 'fill' : 'regular'}
          className={i < full ? '' : 'star-empty'}
          aria-hidden="true"
        />
      ))}
    </span>
  );
}

export function AvailabilityBadge({ hotel }) {
  if (hotel.availableRooms === null || hotel.availableRooms === undefined) {
    return <span className="badge">Check dates</span>;
  }
  if (hotel.availableRooms <= 0) {
    return <span className="badge badge-bad" role="status">Sold out</span>;
  }
  if (hotel.availableRooms <= 3) {
    return <span className="badge badge-warn" role="status">Only {hotel.availableRooms} left</span>;
  }
  return <span className="badge badge-ok" role="status">Available</span>;
}

export default function HotelCard({ hotel, checkIn, checkOut, guests }) {
  const [saved, setSaved] = useState(false);
  const detailLink = `/hotels/${hotel.id}?checkIn=${checkIn || ''}&checkOut=${checkOut || ''}&guests=${guests || 2}`;
  const visibleAmenities = (hotel.amenities || []).slice(0, 4);
  const extraCount = Math.max(0, (hotel.amenities || []).length - visibleAmenities.length);
  const nights = hotel.nights ?? null;
  const total = hotel.totalForStay ?? (nights ? hotel.pricePerNight * nights : null);
  const imgUrl = hotel.thumbnail || hotel.mainPhoto || FALLBACK_IMAGES[(hotel.id || 0) % FALLBACK_IMAGES.length];
  const guestScore = hotel.guestRating > 0 ? hotel.guestRating : null;
  const starClass = hotel.starRating > 0 ? hotel.starRating : null;
  const reviewCount = hotel.reviewCount > 0 ? hotel.reviewCount : null;
  const propertyLabel = hotel.brand
    || (hotel.propertyType ? hotel.propertyType.charAt(0).toUpperCase() + hotel.propertyType.slice(1) : 'Hotel');

  return (
    <article className="card hotel-card">
      <div className="hotel-image">
        <div
          className="hotel-image-inner"
          style={{ backgroundImage: `url(${imgUrl})` }}
          role="img"
          aria-label={hotel.name}
        />
        {guestScore != null && guestScore >= 8 && <span className="image-tag">Guest favourite</span>}
        <button
          className={`heart-button${saved ? ' saved' : ''}`}
          type="button"
          aria-label={saved ? `Remove ${hotel.name} from saved` : `Save ${hotel.name}`}
          onClick={(e) => { e.preventDefault(); setSaved(s => !s); }}
        >
          <Heart
            size={16}
            weight={saved ? 'fill' : 'regular'}
            aria-hidden="true"
          />
        </button>
      </div>

      <div className="hotel-card-body">
        <div className="hotel-card-top">
          <div style={{ minWidth: 0 }}>
            <p className="hotel-type">{propertyLabel}</p>
            <h2 className="hotel-name">
              <Link to={detailLink}>{hotel.name}</Link>
            </h2>
          </div>
          <AvailabilityBadge hotel={hotel} />
        </div>

        {(guestScore != null || starClass != null) && (
          <div className="hotel-rating">
            {guestScore != null && <span className="rating-score">{Number(guestScore).toFixed(1)}</span>}
            {starClass != null && <StarRating rating={starClass} />}
            {reviewCount != null && <span className="rating-count">({reviewCount.toLocaleString()})</span>}
          </div>
        )}

        <p className="hotel-location">
          <MapPin size={12} weight="regular" aria-hidden="true" />
          {hotel.address || hotel.location}
        </p>

        <p className="hotel-desc">{hotel.description}</p>

        {(hotel.roomName || hotel.boardName) && (
          <p className="muted small" style={{ margin: 'var(--space-1) 0' }}>
            {[hotel.roomName, hotel.boardName].filter(Boolean).join(' · ')}
          </p>
        )}

        {visibleAmenities.length > 0 && (
          <ul className="chips" aria-label="Amenities">
            {visibleAmenities.map(a => <li key={a} className="chip">{a}</li>)}
            {extraCount > 0 && <li className="chip">+{extraCount} more</li>}
          </ul>
        )}

        <div className="hotel-card-footer">
          <div>
            <div className="price">{formatMoney(hotel.pricePerNight)}</div>
            <div className="price-sub">per night · taxes incl.</div>
            {total != null && nights != null && (
              <div className="muted small" style={{ marginTop: '2px' }}>
                {formatMoney(total)} total · {nights} night{nights === 1 ? '' : 's'}
              </div>
            )}
          </div>
          <Link to={detailLink} className="btn btn-primary btn-small">
            View deal
          </Link>
        </div>
      </div>
    </article>
  );
}
