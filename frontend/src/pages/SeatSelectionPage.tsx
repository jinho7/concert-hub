import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api';
import type { ApiResponse } from '../api';
import type { Event, Seat, Reservation } from '../types';


const SeatSelectionPage = () => {
  const { id } = useParams<{ id: string }>();
  const [event, setEvent] = useState<Event | null>(null);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedSeat, setSelectedSeat] = useState<Seat | null>(null);
  const [loading, setLoading] = useState(true);
  const [reserving, setReserving] = useState(false);


  useEffect(() => {
    if (id) {
      fetchEventAndSeats(id);
    }
  }, [id]);

  const fetchEventAndSeats = async (eventId: string) => {
    try {
      setLoading(true);
      const [eventResponse, seatsResponse] = await Promise.all([
        api.get<ApiResponse<Event>>(`/events/${eventId}`),
        api.get<ApiResponse<Seat[]>>(`/events/${eventId}/seats`)
      ]);
      
      setEvent(eventResponse.data.data);
      setSeats(seatsResponse.data.data);
    } catch (error) {
      console.error('데이터 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const getSeatColor = (seat: Seat) => {
    switch (seat.status) {
      case 'AVAILABLE':
        return selectedSeat?.id === seat.id 
          ? 'bg-blue-600 text-white' 
          : 'bg-green-100 text-green-800 hover:bg-green-200';
      case 'TEMPORARILY_RESERVED':
        return 'bg-yellow-100 text-yellow-800';
      case 'RESERVED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const handleSeatClick = (seat: Seat) => {
    if (seat.status === 'AVAILABLE') {
      setSelectedSeat(selectedSeat?.id === seat.id ? null : seat);
    }
  };

  if (loading) {
    return <div className="flex justify-center py-12">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
    </div>;
  }

  if (!event) {
    return <div className="text-center py-12">
      <p className="text-red-600">이벤트를 찾을 수 없습니다.</p>
    </div>;
  }

  // 좌석을 행별로 그룹화
  const seatsByRow = seats.reduce((acc, seat) => {
    if (!acc[seat.seatRow]) acc[seat.seatRow] = [];
    acc[seat.seatRow].push(seat);
    return acc;
  }, {} as Record<string, Seat[]>);

  const handleReservation = async () => {
  if (!selectedSeat) return;

  // 로그인 사용자 확인
  const currentUser = localStorage.getItem('currentUser');
  if (!currentUser) {
    alert('예약하려면 먼저 회원가입을 해주세요!');
    window.location.href = '/register';
    return;
  }

  const user = JSON.parse(currentUser);
  setReserving(true);

  try {
    const reservationData = {
      eventId: parseInt(id!),
      seatId: selectedSeat.id,
      userId: user.id
    };

    const response = await api.post<ApiResponse<Reservation>>('/reservations', reservationData);
    const reservation = response.data.data;
    
    alert(`예약이 생성되었습니다! 15분 이내에 결제를 완료해주세요.`);
    
    // 예약 페이지로 이동
    window.location.href = `/reservations/${reservation.id}`;
    
  } catch (error: any) {
    console.error('예약 실패:', error);
    const errorMessage = error.response?.data?.message || '예약에 실패했습니다.';
    alert(errorMessage);
    
    // 실패 시 좌석 정보 새로고침
    fetchEventAndSeats(id!);
  } finally {
    setReserving(false);
  }
};

  return (
    <div className="max-w-4xl mx-auto">
      {/* 이벤트 정보 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h2 className="text-2xl font-bold mb-4">{event.title}</h2>
        <div className="grid grid-cols-2 gap-4 text-sm text-gray-600">
          <div>📍 {event.venue}</div>
          <div>📅 {new Date(event.eventDateTime).toLocaleString('ko-KR')}</div>
          <div>🪑 잔여석: {event.availableSeats}/{event.totalSeats}</div>
          <div>💰 {event.price.toLocaleString()}원~</div>
        </div>
      </div>

      {/* 좌석 범례 */}
      <div className="bg-white rounded-lg shadow-md p-4 mb-6">
        <h3 className="font-semibold mb-3">좌석 상태</h3>
        <div className="flex gap-4 text-sm">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-green-100 border rounded"></div>
            <span>예약 가능</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-yellow-100 border rounded"></div>
            <span>임시 예약</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-red-100 border rounded"></div>
            <span>예약 완료</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-blue-600 border rounded"></div>
            <span>선택됨</span>
          </div>
        </div>
      </div>

      {/* 좌석 맵 */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="text-center mb-6">
          <div className="bg-gray-800 text-white py-2 px-8 rounded-lg inline-block">
            🎭 무대
          </div>
        </div>

        <div className="space-y-4">
          {Object.entries(seatsByRow)
            .sort(([a], [b]) => a.localeCompare(b))
            .map(([row, rowSeats]) => (
              <div key={row} className="flex items-center gap-2">
                <div className="w-8 text-center font-semibold">{row}</div>
                <div className="flex gap-1">
                  {rowSeats
                    .sort((a, b) => parseInt(a.seatNumber) - parseInt(b.seatNumber))
                    .map((seat) => (
                      <button
                        key={seat.id}
                        onClick={() => handleSeatClick(seat)}
                        disabled={seat.status !== 'AVAILABLE'}
                        className={`w-8 h-8 text-xs rounded ${getSeatColor(seat)} 
                          ${seat.status === 'AVAILABLE' ? 'cursor-pointer' : 'cursor-not-allowed'}
                          transition-colors border`}
                        title={`${seat.seatDisplay} (${seat.price.toLocaleString()}원)`}
                      >
                        {seat.seatNumber}
                      </button>
                    ))
                  }
                </div>
              </div>
            ))
          }
        </div>
      </div>

      {/* 선택된 좌석 정보 */}
      {selectedSeat && (
        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <h3 className="font-semibold mb-4">선택된 좌석</h3>
          <div className="flex justify-between items-center">
            <div>
              <p className="text-lg font-semibold">{selectedSeat.seatDisplay}</p>
              <p className="text-gray-600">{selectedSeat.price.toLocaleString()}원</p>
            </div>
            <button
              onClick={handleReservation}
              disabled={reserving}
              className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 disabled:opacity-50"
            >
              {reserving ? '예약 중...' : '예약하기'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default SeatSelectionPage;