import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../api/client';
import type { ApiResponse } from '../types/auth';
import type { Event } from '../types';

const EventListPage = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get<ApiResponse<Event[]>>('/events');
      setEvents(response.data.data);
    } catch (err) {
      setError('이벤트를 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex justify-center items-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">이벤트를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex justify-center items-center">
        <div className="text-center">
          <div className="text-6xl mb-4">😵</div>
          <p className="text-red-600 mb-4">{error}</p>
          <button
            onClick={fetchEvents}
            className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 네비게이션 바 */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Link to="/" className="text-2xl font-bold text-gray-900">
                🎫 Concert Hub
              </Link>
            </div>
            <div className="flex items-center space-x-4">
              <Link
                to="/events"
                className="text-blue-600 hover:text-blue-800 px-3 py-2 rounded-md text-sm font-medium"
              >
                이벤트 목록
              </Link>
              <button
                onClick={handleLogout}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium"
              >
                로그아웃
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* 메인 컨텐츠 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">🎵 진행 중인 이벤트</h2>
          <p className="text-gray-600">원하시는 이벤트를 선택하여 좌석을 예약하세요.</p>
        </div>
        
        {events.length === 0 ? (
          <div className="text-center py-12">
            <div className="bg-white rounded-lg shadow-md p-8">
              <div className="text-6xl mb-4">🎭</div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">진행 중인 이벤트가 없습니다</h3>
              <p className="text-gray-500 mb-6">새로운 이벤트가 곧 등록될 예정입니다.</p>
              <button
                onClick={fetchEvents}
                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-md font-medium"
              >
                새로고침
              </button>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map((event) => (
              <div
                key={event.id}
                className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow p-6"
              >
                <div className="flex justify-between items-start mb-4">
                  <h3 className="text-xl font-semibold text-gray-900">{event.title}</h3>
                  <span
                    className={`px-2 py-1 rounded-full text-xs font-medium ${
                      event.status === 'OPEN'
                        ? 'bg-green-100 text-green-800'
                        : event.status === 'SOLD_OUT'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {event.status === 'OPEN' ? '예매중' : event.status === 'SOLD_OUT' ? '매진' : '마감'}
                  </span>
                </div>
                
                <p className="text-gray-600 mb-4">{event.description}</p>
                
                <div className="space-y-2 mb-4">
                  <div className="flex items-center text-sm text-gray-500">
                    <span className="font-medium">📍 장소:</span>
                    <span className="ml-1">{event.venue}</span>
                  </div>
                  <div className="flex items-center text-sm text-gray-500">
                    <span className="font-medium">📅 일시:</span>
                    <span className="ml-1">
                      {new Date(event.eventDateTime).toLocaleString('ko-KR')}
                    </span>
                  </div>
                  <div className="flex items-center text-sm text-gray-500">
                    <span className="font-medium">💰 가격:</span>
                    <span className="ml-1">{event.price.toLocaleString()}원~</span>
                  </div>
                  <div className="flex items-center text-sm text-gray-500">
                    <span className="font-medium">🪑 잔여석:</span>
                    <span className="ml-1">{event.availableSeats} / {event.totalSeats}</span>
                  </div>
                </div>
                
                <Link
                  to={`/events/${event.id}/seats`}
                  className={`block w-full py-2 px-4 rounded font-medium transition-colors text-center ${
                    event.status === 'OPEN'
                      ? 'bg-blue-600 text-white hover:bg-blue-700'
                      : 'bg-gray-300 text-gray-500 cursor-not-allowed pointer-events-none'
                  }`}
                >
                  {event.status === 'OPEN' ? '좌석 선택' : '예매 불가'}
                </Link>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default EventListPage;