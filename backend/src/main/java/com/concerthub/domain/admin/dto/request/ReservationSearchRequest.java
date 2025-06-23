package com.concerthub.domain.admin.dto.request;

import com.concerthub.domain.reservation.entity.status.ReservationStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationSearchRequest {

    private Long eventId;
    private ReservationStatus status;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    private String userEmail;
    private String userName;
    
    // 페이지네이션
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
