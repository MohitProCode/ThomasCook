import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Header() {
  const { user, logout } = useAuth();
  return (
    <header className="site-header no-print">
      <div className="container header-inner">
        <Link to="/" className="brand">
          <span className="brand-mark" aria-hidden="true">TC</span>
          <span>Thomas Cook <span className="brand-sub">Hotels</span></span>
        </Link>
        <nav className="nav">
          <a href="#support">Help</a>
          <NavLink to="/" end>
            Explore stays
          </NavLink>
          <NavLink to="/my-booking">My trips</NavLink>
          {user ? <><NavLink className="nav-user" to="/account"><span className="avatar">{(user.displayName || user.email).slice(0,1).toUpperCase()}</span><span>{user.displayName}</span></NavLink><button className="nav-account" type="button" onClick={logout}>Sign out</button></> : <NavLink className="nav-account" to="/login">Sign in</NavLink>}
        </nav>
      </div>
    </header>
  );
}
