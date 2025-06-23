package com.concerthub.domain.reservation.entity.status;

public enum ReservationStatus {
    PENDING,     // 임시 예약 (결제 대기)
    CONFIRMED,   // 예약 확정 (결제 완료)
    CANCELLED,   // 예약 취소
    EXPIRED      // 예약 만료 (15분 초과)
}