package com.concerthub.domain.event.entity.status;

public enum EventStatus {
    OPEN,       // 예약 가능
    SOLD_OUT,   // 매진
    CLOSED,     // 예약 마감
    CANCELLED   // 취소됨
}