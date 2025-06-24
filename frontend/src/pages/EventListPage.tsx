import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/client';
import type { ApiResponse } from '../types/auth';
import type { Event } from '../types';

const EventListPage = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600">{error}</p>
        <button
          onClick={fetchEvents}
          className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div>
      <h2 className="text-3xl font-bold text-gray-900 mb-8">🎵 진행 중인 이벤트</h2>
      
      {events.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500">진행 중인 이벤트가 없습니다.</p>
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
    </div>
  );
};

export default EventListPage;
