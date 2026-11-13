import { useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import { Moon, Sun, List, X } from '@phosphor-icons/react';
import { useAuth } from '../context/AuthContext.jsx';
import { useTheme } from '../context/ThemeContext.jsx';

export default function Header() {
  const { user, logout } = useAuth();
  const { theme, setTheme } = useTheme();
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();

  // Close drawer on navigation
  function close() { setMenuOpen(false); }

  return (
    <header className="site-header no-print">
      <div className="container header-inner">
        <Link to="/" className="brand" onClick={close}>
          <span className="brand-mark" aria-hidden="true">TC</span>
          <span>Thomas Cook <span className="brand-sub">Hotels</span></span>
        </Link>

        {/* Desktop nav */}
        <nav className="nav nav-desktop">
          <NavLink to="/" end>Explore</NavLink>
          <NavLink to="/my-booking">My trips</NavLink>
          <a href="#support">Help</a>

          <button
            type="button"
            className="btn btn-ghost btn-small"
            onClick={() => setTheme(t => t === 'dark' ? 'light' : 'dark')}
            aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
            style={{ padding: '0.375rem', lineHeight: 0 }}
          >
            {theme === 'dark'
              ? <Sun size={18} weight="regular" aria-hidden="true" />
              : <Moon size={18} weight="regular" aria-hidden="true" />
            }
          </button>

          {user ? (
            <>
              <NavLink className="nav-user" to="/account">
                <span className="avatar" aria-hidden="true">
                  {(user.displayName || user.email).slice(0, 1).toUpperCase()}
                </span>
                <span>{user.displayName || user.email.split('@')[0]}</span>
              </NavLink>
              <button className="nav-account" type="button" onClick={logout}>
                Sign out
              </button>
            </>
          ) : (
            <NavLink className="nav-account" to="/login">Sign in</NavLink>
          )}
        </nav>

        {/* Mobile right controls */}
        <div className="nav-mobile-controls">
          <button
            type="button"
            className="btn btn-ghost btn-small"
            onClick={() => setTheme(t => t === 'dark' ? 'light' : 'dark')}
            aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
            style={{ padding: '0.375rem', lineHeight: 0 }}
          >
            {theme === 'dark'
              ? <Sun size={18} weight="regular" aria-hidden="true" />
              : <Moon size={18} weight="regular" aria-hidden="true" />
            }
          </button>
          <button
            type="button"
            className="btn btn-ghost btn-small nav-hamburger"
            onClick={() => setMenuOpen(o => !o)}
            aria-label={menuOpen ? 'Close menu' : 'Open menu'}
            aria-expanded={menuOpen}
            style={{ padding: '0.375rem', lineHeight: 0 }}
          >
            {menuOpen
              ? <X size={20} weight="bold" aria-hidden="true" />
              : <List size={20} weight="bold" aria-hidden="true" />
            }
          </button>
        </div>
      </div>

      {/* Mobile drawer */}
      {menuOpen && (
        <nav className="nav-drawer" aria-label="Mobile navigation">
          <NavLink to="/" end onClick={close}>Explore</NavLink>
          <NavLink to="/my-booking" onClick={close}>My trips</NavLink>
          <a href="#support" onClick={close}>Help</a>
          {user ? (
            <>
              <NavLink to="/account" onClick={close} className="nav-drawer-user">
                <span className="avatar" aria-hidden="true">
                  {(user.displayName || user.email).slice(0, 1).toUpperCase()}
                </span>
                {user.displayName || user.email.split('@')[0]}
              </NavLink>
              <button type="button" className="nav-drawer-signout" onClick={() => { logout(); close(); }}>
                Sign out
              </button>
            </>
          ) : (
            <NavLink to="/login" onClick={close} className="nav-drawer-cta">Sign in</NavLink>
          )}
        </nav>
      )}
    </header>
  );
}
