import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <section>
      <h1>Page not found</h1>
      <p className="muted">That page does not exist.</p>
      <p>
        <Link to="/" className="btn btn-primary">
          Back home
        </Link>
      </p>
    </section>
  );
}
