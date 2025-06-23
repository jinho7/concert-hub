package com.concerthub.domain.seat.entity.status;

public enum SeatStatus {
    AVAILABLE,           // 예약 가능
    TEMPORARILY_RESERVED, // 임시 예약 (15분 TTL)
    RESERVED,            // 예약 완료
    BLOCKED              // 좌석 차단 (판매 불가)
}