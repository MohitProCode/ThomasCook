import { formatMoney } from '../utils/money.js';

export default function PriceBreakdown({ pricePerNight, nights, currency = 'INR' }) {
  const total = Number(pricePerNight || 0) * Number(nights || 0);
  return (
    <div className="price-breakdown">
      <div className="breakdown-row">
        <span>
          {formatMoney(pricePerNight, currency)} × {nights} night{nights === 1 ? '' : 's'}
        </span>
        <span>{formatMoney(total, currency)}</span>
      </div>
      <div className="breakdown-row muted small">
        <span>Taxes and fees</span>
        <span>Included</span>
      </div>
      <div className="breakdown-row breakdown-total">
        <span>Total</span>
        <strong>{formatMoney(total, currency)}</strong>
      </div>
    </div>
  );
}
