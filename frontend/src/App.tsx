import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import EventListPage from './pages/EventListPage';
import SeatSelectionPage from './pages/SeatSelectionPage';
import ReservationPage from './pages/ReservationPage';
import UserRegisterPage from './pages/UserRegisterPage';
import './App.css';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white shadow-sm border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center py-4">
              <h1 className="text-2xl font-bold text-gray-900">🎫 Concert Hub</h1>
              <nav>
                <a href="/" className="text-blue-600 hover:text-blue-800">홈</a>
                <a href="/register" className="ml-4 text-blue-600 hover:text-blue-800">회원가입</a>
              </nav>
            </div>
          </div>
        </header>

        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            <Route path="/" element={<EventListPage />} />
            <Route path="/events/:id/seats" element={<SeatSelectionPage />} />
            <Route path="/reservations/:id" element={<ReservationPage />} />
            <Route path="/register" element={<UserRegisterPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;