import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import EventListPage from './pages/EventListPage';
import SeatSelectionPage from './pages/SeatSelectionPage';
import ReservationPage from './pages/ReservationPage';
import UserRegisterPage from './pages/UserRegisterPage';
import LoginPage from './pages/LoginPage';
import './App.css';

// 네비게이션 컴포넌트
const Navigation: React.FC = () => {
  const { isAuthenticated, logout, isLoading } = useAuth();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error('로그아웃 오류:', error);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center">
        <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-4">
          <Link to="/" className="text-2xl font-bold text-gray-900">
            🎫 Concert Hub
          </Link>
          
          <nav className="flex items-center space-x-4">
            <Link to="/" className="text-blue-600 hover:text-blue-800">
              홈
            </Link>
            
            {isAuthenticated ? (
              <>
                <Link to="/my-tickets" className="text-blue-600 hover:text-blue-800">
                  내 티켓
                </Link>
                <button
                  onClick={handleLogout}
                  className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-blue-600 hover:text-blue-800">
                  로그인
                </Link>
                <Link to="/register" className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
                  회원가입
                </Link>
              </>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
};

// 보호된 라우트 컴포넌트
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

// 메인 앱 내용
const AppContent: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Routes>
          {/* 공개 라우트 */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<UserRegisterPage />} />
          <Route path="/" element={<EventListPage />} />
          
          {/* 보호된 라우트 */}
          <Route
            path="/events/:id/seats"
            element={
              <ProtectedRoute>
                <SeatSelectionPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reservations/:id"
            element={
              <ProtectedRoute>
                <ReservationPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-tickets"
            element={
              <ProtectedRoute>
                <div className="text-center py-16">
                  <h2 className="text-2xl font-bold text-gray-900 mb-4">내 티켓</h2>
                  <p className="text-gray-600">마이페이지 기능은 곧 구현될 예정입니다.</p>
                </div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </div>
  );
};

// 메인 App 컴포넌트
const App: React.FC = () => {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
};

export default App;
