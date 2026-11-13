import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { EnvelopeSimple, Lock, User } from '@phosphor-icons/react';
import { loginAccount, registerAccount } from '../api/auth.js';
import { useAuth } from '../context/AuthContext.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function LoginPage() {
  const [mode, setMode] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const auth = useAuth();
  const navigate = useNavigate();

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const data = mode === 'login'
        ? await loginAccount({ email, password })
        : await registerAccount({ email, password, displayName });
      auth.save(data);
      navigate('/my-booking');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">Thomas Cook Hotels</p>
        <h1>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h1>
        <p className="muted" style={{ marginTop: 'var(--space-1)' }}>
          {mode === 'login'
            ? 'Sign in to manage your bookings.'
            : 'One account for every Thomas Cook booking.'}
        </p>

        <form className="card form" onSubmit={submit} noValidate style={{ marginTop: 'var(--space-5)' }}>
          {mode === 'register' && (
            <div className="field">
              <label htmlFor="displayName">Full name</label>
              <div style={{ position: 'relative' }}>
                <User size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)', pointerEvents: 'none' }} aria-hidden="true" />
                <input
                  id="displayName"
                  type="text"
                  autoComplete="name"
                  required
                  minLength={2}
                  value={displayName}
                  onChange={(e) => setDisplayName(e.target.value)}
                  style={{ paddingLeft: '2.25rem' }}
                />
              </div>
            </div>
          )}

          <div className="field">
            <label htmlFor="email">Email address</label>
            <div style={{ position: 'relative' }}>
              <EnvelopeSimple size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)', pointerEvents: 'none' }} aria-hidden="true" />
              <input
                id="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ paddingLeft: '2.25rem' }}
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="password">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)', pointerEvents: 'none' }} aria-hidden="true" />
              <input
                id="password"
                type="password"
                autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ paddingLeft: '2.25rem' }}
              />
            </div>
          </div>

          {error && <ErrorBanner message={error} />}

          <button className="btn btn-primary btn-block" disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
          </button>

          {mode === 'login' && (
            <p style={{ textAlign: 'center', fontSize: '0.875rem', marginTop: 'var(--space-2)' }}>
              <Link to="/forgot-password" style={{ color: 'var(--muted)' }}>Forgot password?</Link>
            </p>
          )}
        </form>

        <div className="auth-links">
          <span>
            {mode === 'login' ? 'New here? ' : 'Already have an account? '}
            <button type="button" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
              {mode === 'login' ? 'Create account' : 'Sign in'}
            </button>
          </span>
          <Link to="/">Back to stays</Link>
        </div>
      </div>
    </div>
  );
}
