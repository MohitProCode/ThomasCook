import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="site-footer no-print">
      <div className="container">
        <div className="footer-grid">
          <div className="footer-brand">
            <Link to="/" className="brand">
              <span className="brand-mark" aria-hidden="true">TC</span>
              <span style={{ color: '#fff' }}>Thomas Cook <span style={{ color: 'rgba(255,255,255,0.4)', fontWeight: 400, fontSize: '0.85rem' }}>Hotels</span></span>
            </Link>
            <p>Discover extraordinary stays across India's most beautiful destinations. Best price guaranteed.</p>
          </div>

          <div className="footer-col">
            <h4>Destinations</h4>
            <ul>
              <li><Link to="/results?location=Goa&checkIn=2026-12-20&checkOut=2026-12-23&guests=2">Goa</Link></li>
              <li><Link to="/results?location=Jaipur&checkIn=2026-12-24&checkOut=2026-12-26&guests=2">Jaipur</Link></li>
              <li><Link to="/results?location=Manali&checkIn=2026-12-20&checkOut=2026-12-23&guests=2">Manali</Link></li>
              <li><Link to="/">All destinations</Link></li>
            </ul>
          </div>

          <div className="footer-col">
            <h4>Support</h4>
            <ul>
              <li><Link to="/my-booking">Manage booking</Link></li>
              <li><a href="#faq">FAQ</a></li>
              <li><a href="#contact">Contact us</a></li>
              <li><a href="#cancellation">Cancellation policy</a></li>
            </ul>
          </div>

          <div className="footer-col">
            <h4>Company</h4>
            <ul>
              <li><a href="#about">About</a></li>
              <li><a href="#careers">Careers</a></li>
              <li><a href="#press">Press</a></li>
              <li><a href="#privacy">Privacy policy</a></li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} Thomas Cook Hotel Booking Portal — demo build. Prices in INR, taxes included.</span>
          <span style={{ color: 'rgba(255,255,255,0.25)', fontSize: '0.78rem' }}>Powered by Nuitee LiteAPI</span>
        </div>
      </div>
    </footer>
  );
}
