# 📖 Phase 1: 기본 CRUD 및 도메인 모델링

## 목표
- Event 도메인 기본 CRUD API 구현
- JPA Entity 설계 및 관계 설정
- 표준 Spring Boot 아키텍처 적용
- RESTful API 설계 및 응답 형식 통일

## 아키텍처 설계

### 도메인 모델
```
Event (이벤트)
├── id: Long (PK)
├── title: String (제목)
├── description: String (설명)  
├── venue: String (장소)
├── eventDateTime: LocalDateTime (공연 시간)
├── totalSeats: Integer (총 좌석 수)
├── availableSeats: Integer (예약 가능 좌석)
├── price: Integer (가격)
├── status: EventStatus (상태)
├── createdAt: LocalDateTime
└── updatedAt: LocalDateTime
```

### EventStatus
- `OPEN`: 예약 가능
- `SOLD_OUT`: 매진
- `CLOSED`: 예약 마감
- `CANCELLED`: 취소됨

### 패키지 구조
```
com.concerthub.domain.event/
├── Event.java              # 엔티티
├── EventStatus.java        # enum
├── EventRepository.java    # 데이터 접근
├── EventService.java       # 비즈니스 로직
├── EventController.java    # API 컨트롤러
└── dto/
    ├── EventCreateRequest.java
    ├── EventResponse.java
    └── EventUpdateRequest.java
```

## 구현 체크리스트

### 완료된 작업
- [x] 프로젝트 초기 설정
- [x] MySQL Docker 환경 구축
- [x] Event 엔티티 설계

### 진행 중
- [ ] JPA Auditing 설정
- [ ] EventRepository 구현
- [ ] EventService 비즈니스 로직
- [ ] EventController API 구현
- [ ] DTO 클래스 구현
- [ ] 예외 처리 시스템
- [ ] API 테스트

## API 설계

### 엔드포인트
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/events` | 이벤트 목록 조회 |
| POST | `/api/events` | 이벤트 생성 |
| GET | `/api/events/{id}` | 이벤트 상세 조회 |
| PUT | `/api/events/{id}` | 이벤트 수정 |
| DELETE | `/api/events/{id}` | 이벤트 삭제 |

### 응답 형식
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "IU 콘서트",
    "description": "아이유 단독 콘서트",
    "venue": "올림픽공원",
    "eventDateTime": "2025-07-15T19:00:00",
    "totalSeats": 5000,
    "availableSeats": 4850,
    "price": 120000,
    "status": "OPEN",
    "createdAt": "2025-06-23T14:30:00",
    "updatedAt": "2025-06-23T14:30:00"
  },
  "message": "이벤트 조회가 완료되었습니다."
}
```

## 핵심 비즈니스 로직

### Event 엔티티 비즈니스 메서드
```java
// 좌석 감소 (예약 시)
public void decreaseAvailableSeats() {
    if (this.availableSeats <= 0) {
        throw new IllegalStateException("예약 가능한 좌석이 없습니다.");
    }
    this.availableSeats--;
    if (this.availableSeats == 0) {
        this.status = EventStatus.SOLD_OUT;
    }
}

// 좌석 증가 (예약 취소 시)
public void increaseAvailableSeats() {
    if (this.availableSeats >= this.totalSeats) {
        throw new IllegalStateException("가용 좌석이 전체 좌석보다 클 수 없습니다.");
    }
    this.availableSeats++;
    this.status = EventStatus.OPEN;
}
```

## 고민 포인트 및 설계 결정사항

### 1. ERD 설계 근거

#### Event와 Seat 분리 결정
**고민**: Event 테이블에 좌석 정보를 JSON으로 저장 vs 별도 Seat 테이블
**결정**: 별도 Seat 테이블로 분리
**근거**:
- 좌석별 개별 상태 관리 필요 (예약됨, 임시예약, 사용가능)
- 좌석별 가격 차등 적용 가능 (VIP, 일반석)
- 향후 좌석별 예약 이력 추적 시 정규화된 구조 필요
- 동시성 제어 시 좌석 단위 락 적용 가능

#### availableSeats를 Event에 중복 저장하는 이유
**고민**: Seat 테이블에서 COUNT 쿼리로 실시간 계산 vs Event 테이블에 중복 저장
**결정**: Event 테이블에 availableSeats 필드 중복 저장
**근거**:
- 이벤트 목록 조회 시 잔여석 정보가 필수적으로 필요
- COUNT 쿼리는 Seat 테이블 풀스캔으로 성능 이슈 발생 가능
- 대용량 트래픽에서 조회 성능이 데이터 정합성보다 우선
- 예약/취소 시점에만 동기화하면 되므로 관리 복잡도 적음

#### LocalDateTime 선택 근거
**고민**: LocalDateTime vs ZonedDateTime vs Instant
**결정**: LocalDateTime 사용
**근거**:
- 국내 서비스로 단일 타임존(KST) 가정
- ZonedDateTime은 타임존 정보로 인한 저장 공간 오버헤드
- 글로벌 서비스 확장 시 애플리케이션 레벨에서 타임존 변환 처리
- JPA에서 LocalDateTime이 가장 안정적인 매핑 지원

### 2. Entity 설계 방향성
- `@Builder` 패턴 선택: 객체 생성 시 필수 필드 강제 및 가독성 향상
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: JPA 요구사항 충족하면서 무분별한 객체 생성 방지
- 비즈니스 로직을 Entity 내부에 배치하여 도메인 주도 설계 적용

### 3. 상태 관리 전략
- EventStatus enum으로 이벤트 상태를 명시적으로 관리
- availableSeats 변경과 status 업데이트를 원자적으로 처리
- 데이터 정합성을 Entity 레벨에서 보장

### 4. JPA Auditing 도입 결정
- 생성/수정 시간을 애플리케이션 레벨에서 자동 관리
- 수동으로 시간을 설정할 때 발생할 수 있는 휴먼 에러 방지
- 향후 감사(Audit) 기능 확장 시 기반 구조 제공

### 5. 예외 처리 설계
- 비즈니스 규칙 위반을 Entity 레벨에서 즉시 검증
- 명확한 예외 메시지로 디버깅 및 운영 효율성 향상
- 도메인 무결성을 코드 레벨에서 강제

## 다음 단계

1. **JPA Auditing 설정** - `JpaConfig.java` 구성
2. **Repository 구현** - 기본 CRUD + 커스텀 쿼리
3. **Service 구현** - 비즈니스 로직 및 트랜잭션 처리
4. **Controller 구현** - RESTful API 및 validation
5. **예외 처리** - GlobalExceptionHandler 구성

---

**작업 일시**: 2025-06-23  
**작업자**: 김진호  
**브랜치**: feat/event-crud-api