// 이벤트 관련 타입
export interface Event {
  id: number;
  title: string;
  description: string;
  venue: string;
  eventDateTime: string;
  totalSeats: number;
  availableSeats: number;
  price: number;
  status: 'OPEN' | 'SOLD_OUT' | 'CLOSED' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
}

// 좌석 관련 타입
export interface Seat {
  id: number;
  eventId: number;
  seatRow: string;
  seatNumber: string;
  seatDisplay: string;
  price: number;
  status: 'AVAILABLE' | 'TEMPORARILY_RESERVED' | 'RESERVED' | 'BLOCKED';
  temporaryReservedAt: string | null;
  expired: boolean;
}

// 사용자 관련 타입
export interface User {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
}

// 예약 관련 타입
export interface Reservation {
  id: number;
  event: {
    id: number;
    title: string;
    venue: string;
    eventDateTime: string;
  };
  seat: {
    id: number;
    seatRow: string;
    seatNumber: string;
    seatDisplay: string;
    price: number;
  };
  user: {
    id: number;
    name: string;
    email: string;
  };
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED';
  totalPrice: number;
  expiresAt: string | null;
  minutesUntilExpiry: number;
  paymentId: string | null;
}