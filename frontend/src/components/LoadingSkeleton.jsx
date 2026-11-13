export default function LoadingSkeleton({ rows = 3 }) {
  return (
    <div className="skeleton-list" aria-label="Loading" role="status">
      {Array.from({ length: rows }).map((_, i) => (
        <div className="card skeleton" key={i}>
          <div className="skeleton-line wide" />
          <div className="skeleton-line" />
          <div className="skeleton-line short" />
        </div>
      ))}
    </div>
  );
}
