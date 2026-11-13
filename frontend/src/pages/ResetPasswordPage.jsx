import { useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { Lock } from '@phosphor-icons/react';
import { resetPassword } from '../api/auth.js';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const token = params.get('token') || '';
  const [next, setNext] = useState('');
  const [done, setDone] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await resetPassword(token, next);
      setDone(true);
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">Account Recovery</p>
        <h1>Choose a new password</h1>
        <p className="muted" style={{ marginTop: 'var(--space-1)' }}>
          Pick a strong password of at least 8 characters.
        </p>

        {done ? (
          <div className="card" style={{ marginTop: 'var(--space-5)' }}>
            <p className="notice">Password updated. Redirecting to sign in…</p>
          </div>
        ) : (
          <form className="card form" onSubmit={submit} noValidate style={{ marginTop: 'var(--space-5)' }}>
            {!token && (
              <p className="field-error" role="alert">This reset link is missing or invalid.</p>
            )}

            <div className="field">
              <label htmlFor="new-password">New password</label>
              <div style={{ position: 'relative' }}>
                <Lock size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)', pointerEvents: 'none' }} aria-hidden="true" />
                <input
                  id="new-password"
                  type="password"
                  autoComplete="new-password"
                  required
                  minLength={8}
                  value={next}
                  onChange={(e) => setNext(e.target.value)}
                  style={{ paddingLeft: '2.25rem' }}
                />
              </div>
            </div>

            {error && <ErrorBanner message={error} />}

            <button className="btn btn-primary btn-block" disabled={busy || !token}>
              {busy ? 'Updating…' : 'Set new password'}
            </button>
          </form>
        )}

        <div className="auth-links">
          <Link to="/login">Back to sign in</Link>
        </div>
      </div>
    </div>
  );
}
