import { useState } from 'react';
import { Link } from 'react-router-dom';
import { EnvelopeSimple } from '@phosphor-icons/react';
import { forgotPassword } from '../api/auth.js';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [done, setDone] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await forgotPassword(email);
      setDone(true);
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
        <h1>Reset your password</h1>
        <p className="muted" style={{ marginTop: 'var(--space-1)' }}>
          Enter the email on your Thomas Cook account.
        </p>

        {done ? (
          <div className="card" style={{ marginTop: 'var(--space-5)' }}>
            <p className="notice" style={{ marginBottom: 'var(--space-4)' }}>
              If an account exists for that address, we've sent a reset link. Check your inbox and spam folder.
            </p>
            <Link to="/login" className="btn btn-primary btn-block">Back to sign in</Link>
          </div>
        ) : (
          <form className="card form" onSubmit={submit} noValidate style={{ marginTop: 'var(--space-5)' }}>
            <div className="field">
              <label htmlFor="reset-email">Email address</label>
              <div style={{ position: 'relative' }}>
                <EnvelopeSimple size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)', pointerEvents: 'none' }} aria-hidden="true" />
                <input
                  id="reset-email"
                  type="email"
                  required
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{ paddingLeft: '2.25rem' }}
                />
              </div>
            </div>

            {error && <ErrorBanner message={error} />}

            <button className="btn btn-primary btn-block" disabled={busy}>
              {busy ? 'Sending…' : 'Email reset link'}
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
