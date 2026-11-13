export default function ErrorBanner({ message, onRetry, onDismiss }) {
  if (!message) return null;
  return (
    <div className="error-banner" role="alert">
      <span>{message}</span>
      <span className="error-actions">
        {onRetry && (
          <button type="button" className="btn btn-small" onClick={onRetry}>
            Retry
          </button>
        )}
        {onDismiss && (
          <button type="button" className="btn btn-small btn-ghost" onClick={onDismiss}>
            Dismiss
          </button>
        )}
      </span>
    </div>
  );
}
