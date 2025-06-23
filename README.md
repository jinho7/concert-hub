# 🎫 Concert-Hub: Real-time Concert Ticketing Platform

> **대용량 트래픽을 처리하는 고성능 티켓 예약 시스템**  
> 동시 접속자 10,000명을 안정적으로 처리하는 클라우드 네이티브 아키텍처를 구축하는 것이 목표

## 1) 서비스 개요

**Concert-Hub**는 콘서트, 뮤지컬, 스포츠 경기 등의 티켓을 실시간으로 예약할 수 있는 플랫폼입니다.

_현재는 데모 버전(개발 공부용)으로 운영 중_

### 핵심 비즈니스 가치
다음과 같은 로직에 초점을 두고 개발하였습니다.
- **실시간 예약**: 대기열 시스템으로 공정한 티켓 판매
- **안전한 결제**: 예약-결제-발급의 원자적 트랜잭션 보장  
- **고성능**: 순간 트래픽 폭증에도 안정적인 서비스 제공
- **확장성**: 마이크로서비스 아키텍처로 유연한 확장

### 중점적으로 고려한 Issue
- **동시성 이슈**: 같은 좌석 중복 예약 완전 차단
- **성능 병목**: 대량 조회 요청으로 인한 DB 부하 해결
- **시스템 안정성**: 순간 트래픽 폭증 시에도 서비스 보호
- **운영 효율성**: 자동화된 배포와 모니터링으로 안정적 운영

## 🚀 Phase별 발전 과정

| Phase | 목표 | 핵심 기술 | 기간 |
|-------|------|-----------|------|
| **Phase 1** | MVP + 동시성 제어 | Spring Boot, MySQL, 비관적 락 | 1주 |
| **Phase 2** | 성능 최적화 | Redis 분산 락, 캐싱, 인덱싱 | 1주 |
| **Phase 3** | 인프라 현대화 | Docker, CI/CD, AWS 3-tier | 1주 |
| **Phase 4** | 대용량 처리 | 대기열, WebSocket, 이벤트 기반 | 1주 |
| **Phase 5** | 클라우드 네이티브 | EKS, ArgoCD, Terraform, 모니터링 | 1.5주 |

## 🏗️ 아키텍처 진화

### Phase 1: 단일 서버 MVP
[Client] → [Spring Boot + MySQL]

### Phase 2-3: 수평 확장
[Client] → [ALB] → [Spring Boot Cluster] → [MySQL + Redis]

### Phase 5: 클라우드 네이티브
[Client] → [Ingress] → [K8s Services] → [Pods] → [AWS RDS + ElastiCache]
↓
[ArgoCD] ← [GitOps] ← [GitHub Actions]
↓
[Prometheus + Grafana] → [Monitoring]

## 📚 기술 스택

### Backend
- **Framework**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: MySQL 8.0, Redis 7.0
- **Messaging**: Spring Events, WebSocket + STOMP
- **Testing**: JUnit 5, Testcontainers, JMeter

### Infrastructure
- **Cloud**: AWS (EC2, RDS, ElastiCache, ALB, EKS)
- **Container**: Docker, Kubernetes, Helm
- **IaC**: Terraform, AWS CDK
- **CI/CD**: GitHub Actions, ArgoCD
- **Monitoring**: Prometheus, Grafana, CloudWatch

### Frontend (간단한 UI)
- **Framework**: React 18, TypeScript
- **State**: Zustand
- **Styling**: Tailwind CSS

## 📖 문서화

각 Phase별 상세 문서는 `docs/` 폴더에서 확인하실 수 있습니다.

- [Phase 1: MVP 구현](./docs/phase-1/README.md)
- [Phase 2: 성능 최적화](./docs/phase-2/README.md)
- [Phase 3: 인프라 현대화](./docs/phase-3/README.md)
- [Phase 4: 대용량 처리](./docs/phase-4/README.md)
- [Phase 5: 클라우드 네이티브](./docs/phase-5/README.md)

## 🎬 Getting Started

### 개발 환경 요구사항
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- AWS CLI
- kubectl

### 로컬 실행
```bash
# Phase별 실행 방법은 각 문서 참조
cd backend
./gradlew bootRun
```

🧑‍💻 Developer: 김진호 (Kim Jinho)

📧 Contact: jh7524jh7524@gmail.com

🔗 GitHub: https://github.com/jinho7

"단순히 작동하는 코드가 아닌, 왜 그렇게 설계했는지 설명할 수 있는 개발자"
