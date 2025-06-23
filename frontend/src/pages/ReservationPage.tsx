import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api';
import type { ApiResponse } from '../api';
import type { Reservation } from '../types';

const ReservationPage = () => {
  const { id } = useParams<{ id: string }>();
  const [reservation, setReservation] = useState<Reservation | null>(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    if (id) {
      fetchReservation(id);
      
      // 1분마다 예약 상태 갱신
      const interval = setInterval(() => fetchReservation(id), 60000);
      return () => clearInterval(interval);
    }
  }, [id]);

  const fetchReservation = async (reservationId: string) => {
    try {
      const response = await api.get<ApiResponse<Reservation>>(`/reservations/${reservationId}`);
      setReservation(response.data.data);
    } catch (error) {
      console.error('예약 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePayment = async () => {
    if (!reservation) return;
    
    setProcessing(true);
    try {
      // 결제 모킹 API 호출
      await api.post(`/payments/mock/${reservation.id}`);
      
      // 예약 확정
      const response = await api.post<ApiResponse<Reservation>>(
        `/reservations/${reservation.id}/confirm`,
        { paymentId: `MOCK_PAYMENT_${Date.now()}` }
      );
      
      setReservation(response.data.data);
      alert('결제가 완료되어 예약이 확정되었습니다!');
      
    } catch (error: any) {
      console.error('결제 실패:', error);
      const errorMessage = error.response?.data?.message || '결제에 실패했습니다.';
      alert(errorMessage);
    } finally {
      setProcessing(false);
    }
  };

  const handleCancel = async () => {
    if (!reservation || !confirm('정말 예약을 취소하시겠습니까?')) return;
    
    setProcessing(true);
    try {
      const response = await api.delete<ApiResponse<Reservation>>(`/reservations/${reservation.id}/cancel`);
      setReservation(response.data.data);
      alert('예약이 취소되었습니다.');
    } catch (error: any) {
      console.error('취소 실패:', error);
      alert(error.response?.data?.message || '취소에 실패했습니다.');
    } finally {
      setProcessing(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <span className="bg-yellow-100 text-yellow-800 px-2 py-1 rounded-full text-sm">결제 대기</span>;
      case 'CONFIRMED':
        return <span className="bg-green-100 text-green-800 px-2 py-1 rounded-full text-sm">예약 확정</span>;
      case 'CANCELLED':
        return <span className="bg-red-100 text-red-800 px-2 py-1 rounded-full text-sm">예약 취소</span>;
      case 'EXPIRED':
        return <span className="bg-gray-100 text-gray-800 px-2 py-1 rounded-full text-sm">예약 만료</span>;
      default:
        return <span className="bg-gray-100 text-gray-800 px-2 py-1 rounded-full text-sm">{status}</span>;
    }
  };

  if (loading) {
    return <div className="flex justify-center py-12">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
    </div>;
  }

  if (!reservation) {
    return <div className="text-center py-12">
      <p className="text-red-600">예약을 찾을 수 없습니다.</p>
    </div>;
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="flex justify-between items-start mb-6">
          <h2 className="text-2xl font-bold">예약 확인</h2>
          {getStatusBadge(reservation.status)}
        </div>

        {/* 이벤트 정보 */}
        <div className="border-b pb-4 mb-4">
          <h3 className="font-semibold text-lg mb-2">{reservation.event.title}</h3>
          <div className="space-y-1 text-gray-600">
            <p>📍 {reservation.event.venue}</p>
            <p>📅 {new Date(reservation.event.eventDateTime).toLocaleString('ko-KR')}</p>
          </div>
        </div>

        {/* 좌석 정보 */}
        <div className="border-b pb-4 mb-4">
          <h3 className="font-semibold mb-2">좌석 정보</h3>
          <div className="flex justify-between">
            <span>좌석: {reservation.seat.seatDisplay}</span>
            <span className="font-semibold">{reservation.totalPrice.toLocaleString()}원</span>
          </div>
        </div>

        {/* 예약자 정보 */}
        <div className="border-b pb-4 mb-4">
          <h3 className="font-semibold mb-2">예약자 정보</h3>
          <div className="space-y-1 text-gray-600">
            <p>이름: {reservation.user.name}</p>
            <p>이메일: {reservation.user.email}</p>
          </div>
        </div>

        {/* 결제 대기 중인 경우 */}
        {reservation.status === 'PENDING' && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-4">
            <h3 className="font-semibold text-yellow-800 mb-2">⏰ 결제 대기 중</h3>
            <p className="text-yellow-700 mb-3">
              {reservation.minutesUntilExpiry > 0 
                ? `${reservation.minutesUntilExpiry}분 후 예약이 만료됩니다.`
                : '예약이 만료되었습니다.'
              }
            </p>
            {reservation.minutesUntilExpiry > 0 && (
              <div className="flex gap-2">
                <button
                  onClick={handlePayment}
                  disabled={processing}
                  className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:opacity-50"
                >
                  {processing ? '결제 중...' : '결제하기'}
                </button>
                <button
                  onClick={handleCancel}
                  disabled={processing}
                  className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 disabled:opacity-50"
                >
                  취소하기
                </button>
              </div>
            )}
          </div>
        )}

        {/* 확정된 경우 */}
        {reservation.status === 'CONFIRMED' && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-4">
            <h3 className="font-semibold text-green-800 mb-2">✅ 예약 완료</h3>
            <p className="text-green-700">결제가 완료되어 예약이 확정되었습니다!</p>
            {reservation.paymentId && (
              <p className="text-sm text-green-600 mt-1">결제 ID: {reservation.paymentId}</p>
            )}
          </div>
        )}

        {/* 하단 버튼 */}
        <div className="flex gap-2">
          <button
            onClick={() => window.location.href = '/'}
            className="flex-1 bg-gray-500 text-white py-2 rounded hover:bg-gray-600"
          >
            이벤트 목록으로
          </button>
          {reservation.status === 'CONFIRMED' && (
            <button
              onClick={handleCancel}
              disabled={processing}
              className="flex-1 bg-red-500 text-white py-2 rounded hover:bg-red-600 disabled:opacity-50"
            >
              예약 취소
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ReservationPage;