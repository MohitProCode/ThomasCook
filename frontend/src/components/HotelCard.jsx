import { Link } from 'react-router-dom';
import { formatMoney } from '../utils/money.js';

export function AvailabilityBadge({ hotel }) {
  if (hotel.availableRooms === null || hotel.availableRooms === undefined) {
    return <span className="badge">Check dates for availability</span>;
  }
  if (hotel.availableRooms <= 0) {
    return (
      <span className="badge badge-bad" role="status">
        Sold out
      </span>
    );
  }
  if (hotel.availableRooms <= 3) {
    return (
      <span className="badge badge-warn" role="status">
        Only {hotel.availableRooms} left
      </span>
    );
  }
  return (
    <span className="badge badge-ok" role="status">
      Available
    </span>
  );
}

export default function HotelCard({ hotel, checkIn, checkOut, guests }) {
  const detailLink = `/hotels/${hotel.id}?checkIn=${checkIn || ''}&checkOut=${checkOut || ''}&guests=${guests || 2}`;
  const visibleAmenities = (hotel.amenities || []).slice(0, 5);
  const extraCount = Math.max(0, (hotel.amenities || []).length - visibleAmenities.length);
  const nights = hotel.nights ?? null;
  const total = hotel.totalForStay ?? (nights ? hotel.pricePerNight * nights : null);

  return (
    <article className="card hotel-card">
      <div className="hotel-image" style={{ backgroundImage: `url(${hotel.imageUrl || `https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=80`})` }}>
        <span className="image-tag">Guest favourite</span>
        <button className="heart-button" type="button" aria-label={`Save ${hotel.name}`}>♡</button>
      </div>
      <div className="hotel-card-top">
        <div><p className="hotel-type">HOTEL</p><h2 className="hotel-name"><Link to={detailLink}>{hotel.name}</Link></h2></div>
        <AvailabilityBadge hotel={hotel} />
      </div>
      <p className="hotel-location">⌖ {hotel.location} · Great location</p>
      <p className="hotel-desc">{hotel.description}</p>
      {visibleAmenities.length > 0 && (
        <ul className="chips" aria-label="Amenities">
          {visibleAmenities.map((amenity) => (
            <li key={amenity} className="chip">
              {amenity}
            </li>
          ))}
          {extraCount > 0 && <li className="chip chip-more">+{extraCount}</li>}
        </ul>
      )}
      <div className="hotel-card-footer">
        <div>
          <div className="price">{formatMoney(hotel.pricePerNight)}</div>
          <div className="muted small">per night</div>
          {total != null && nights != null && (
            <div className="muted small">
              {formatMoney(total)} for {nights} night{nights === 1 ? '' : 's'}
            </div>
          )}
        </div>
        <Link to={detailLink} className="btn btn-primary">
          See availability
        </Link>
      </div>
    </article>
  );
}
