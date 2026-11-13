export default function LoadingSkeleton({ rows = 3 }) {
  return (
    <div className="skeleton-list results-grid" aria-label="Loading" role="status">
      {Array.from({ length: rows }).map((_, i) => (
        <div className="card skeleton" key={i} style={{ padding: 0, overflow: 'hidden' }}>
          <div className="skeleton-img" />
          <div className="skeleton-body">
            <div className="skeleton-line tall wide" />
            <div className="skeleton-line" />
            <div className="skeleton-line short" />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '.5rem' }}>
              <div className="skeleton-line" style={{ width: '30%', height: '1.4rem' }} />
              <div className="skeleton-line" style={{ width: '28%', height: '1.4rem', borderRadius: '999px' }} />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
