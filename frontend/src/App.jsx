import { Routes, Route } from 'react-router-dom';
import Header from './components/Header.jsx';
import Footer from './components/Footer.jsx';
import SearchPage from './pages/SearchPage.jsx';
import ResultsPage from './pages/ResultsPage.jsx';
import HotelDetailPage from './pages/HotelDetailPage.jsx';
import ConfirmationPage from './pages/ConfirmationPage.jsx';
import MyBookingPage from './pages/MyBookingPage.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import AccountPage from './pages/AccountPage.jsx';
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx';
import ResetPasswordPage from './pages/ResetPasswordPage.jsx';

export default function App() {
  return (
    <div className="app">
      <Header />
      <main className="container main">
        <Routes>
          <Route path="/" element={<SearchPage />} />
          <Route path="/results" element={<ResultsPage />} />
          <Route path="/hotels/:id" element={<HotelDetailPage />} />
          <Route path="/confirmation/:reference" element={<ConfirmationPage />} />
          <Route path="/my-booking" element={<MyBookingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/account" element={<AccountPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
