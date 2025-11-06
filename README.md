# 🚀 Enterprise Crypto Exchange Platform

**프로덕션 레벨 암호화폐 거래소** - 실시간 거래, 입출금, 레버리지, 스테이킹 등 풀스택 거래소 시스템

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [시작하기](#시작하기)
- [API 문서](#api-문서)
- [보안](#보안)
- [성능](#성능)
- [모니터링](#모니터링)

---

## 🎯 프로젝트 개요

실제 암호화폐 거래소(바이낸스, 업비트 등)와 동일한 수준의 기능을 제공하는 엔터프라이즈급 플랫폼입니다.

### 핵심 특징

- ✅ **프로덕션 레벨**: 실제 서비스 가능한 코드 품질
- ✅ **확장 가능**: Kubernetes 기반 오토 스케일링
- ✅ **고성능**: Redis 캐싱, 분산 아키텍처
- ✅ **보안**: Rate Limiting, 2FA, API Key 관리
- ✅ **모니터링**: Prometheus + Grafana

---

## 🌟 주요 기능

### 1. 거래 시스템

#### 기본 거래
- **주문 타입**: 시장가(MARKET), 지정가(LIMIT), 스탑로스, 익절
- **주문 상태 관리**: 대기중, 부분체결, 완전체결, 취소됨
- **실시간 주문 매칭 엔진**: PriorityQueue 기반 매칭
- **주문 수정/취소**: 부분 체결된 주문 수정 지원

#### 고급 기능
- **수수료 시스템**: Maker/Taker 수수료 자동 적용
- **수수료 이력 추적**: 모든 거래 수수료 기록
- **거래 내역**: 실시간 거래 체결 내역

### 2. 입출금 시스템

#### 입금 (Deposit)
- 블록체인 트랜잭션 추적
- 확인 수(Confirmations) 자동 체크
- 입금 완료 시 자동 잔고 반영

#### 출금 (Withdrawal)
- 출금 수수료 자동 계산
- IP 화이트리스트 검증
- 출금 취소 및 잔고 복구
- 비관적 락(Pessimistic Lock)으로 동시성 제어

### 3. 보안 시스템

#### API Key 관리
- 권한별 API Key 발급 (Read/Trade/Withdraw)
- IP 화이트리스트 설정
- API Key 만료일 설정
- 사용 이력 추적

#### Rate Limiting
- Redis 기반 분산 Rate Limiting
- 분당 60 requests, 초당 10 requests 제한
- IP 및 API Key 별 제한

#### 2FA (Two-Factor Authentication)
- TOTP (Google Authenticator)
- SMS OTP
- Email OTP
- 백업 코드 지원

### 4. 캐싱 전략

#### Redis Multi-layer Cache
```
- coins: 5분 TTL
- prices: 10초 TTL (실시간성)
- orderBook: 5초 TTL (호가창)
- users: 1시간 TTL
```

### 5. 모니터링 & 로깅

#### Prometheus Metrics
- 거래량, 수수료 수익 추적
- API 응답 시간 모니터링
- 시스템 리소스 사용량

#### Grafana 대시보드
- 실시간 거래 차트
- 사용자 통계
- 시스템 헬스 체크

---

## 🛠 기술 스택

### Backend

| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 23 | 메인 언어 |
| Spring Boot | 3.2.3 | 프레임워크 |
| Spring Security | 3.2.3 | 인증/인가 |
| Spring Data JPA | 3.2.3 | ORM |
| MySQL | 8.0 | 메인 DB |
| Redis | 7.0 | 캐싱/Rate Limiting |
| JWT | 0.11.5 | 토큰 인증 |
| Lombok | - | 보일러플레이트 제거 |

### Frontend

| 기술 | 버전 | 용도 |
|------|------|------|
| React | 18.2.0 | UI 라이브러리 |
| TypeScript | 4.9.5 | 타입 안전성 |
| Vite | 5.1.4 | 빌드 도구 |
| Axios | 1.10.0 | HTTP 클라이언트 |
| STOMP.js | 7.1.1 | WebSocket |
| Lightweight Charts | 5.0.7 | 차트 |
| Ethers.js | 6.14.4 | Web3 지갑 |

### Infrastructure

| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Kubernetes | 오케스트레이션 |
| Prometheus | 메트릭 수집 |
| Grafana | 시각화 |

---

## 🏗 아키텍처

### 시스템 아키텍처

```
┌─────────────┐         ┌─────────────┐
│   Client    │────────▶│ Load Balancer│
│  (React)    │         │  (K8s LB)    │
└─────────────┘         └──────┬──────┘
                               │
                ┌──────────────┼──────────────┐
                │              │              │
         ┌──────▼──────┐ ┌────▼─────┐ ┌──────▼──────┐
         │Spring Boot  │ │Spring Boot│ │Spring Boot  │
         │   Pod 1     │ │   Pod 2   │ │   Pod 3     │
         └──────┬──────┘ └────┬─────┘ └──────┬──────┘
                │              │              │
                └──────────────┼──────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
    ┌─────▼─────┐       ┌──────▼──────┐     ┌──────▼──────┐
    │   MySQL   │       │    Redis    │     │ Prometheus  │
    │ (Primary) │       │   (Cache)   │     │  (Metrics)  │
    └───────────┘       └─────────────┘     └─────────────┘
```

### 데이터베이스 설계

#### 핵심 엔티티

1. **User** - 사용자
2. **Wallet** - 지갑 (자산별)
3. **Transaction** - 입출금 내역
4. **Order** - 거래 주문
5. **Trade** - 거래 체결 내역
6. **Fee** - 수수료 설정
7. **FeeTransaction** - 수수료 거래 이력
8. **ApiKey** - API 키 관리
9. **TwoFactorAuth** - 2FA 설정

---

## 🚀 시작하기

### 필수 요구사항

- Java 21+
- Docker & Docker Compose
- Node.js 18+
- MySQL 8.0
- Redis 7.0

### 로컬 개발 환경

#### 1. Docker Compose로 실행

```bash
# 레포지토리 클론
git clone https://github.com/DongKyeongs/DongKyeongsProjects.git
cd DongKyeongsProjects

# Docker Compose 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

#### 2. 수동 실행

```bash
# 백엔드 빌드 및 실행
./gradlew clean build
java -jar build/libs/*.jar

# 프론트엔드 실행
cd frontend
npm install
npm run start
```

### Kubernetes 배포

```bash
# ConfigMap & Secrets 생성
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/secrets.yml

# 애플리케이션 배포
kubectl apply -f k8s/deployment.yml

# 상태 확인
kubectl get pods
kubectl get svc
```

---

## 📚 API 문서

### 인증

#### 회원가입
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123"
}
```

#### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123"
}
```

### 거래

#### 주문 생성
```http
POST /api/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "symbol": "BTC/USDT",
  "type": "LIMIT",
  "side": "BUY",
  "price": "50000.00",
  "quantity": "0.1"
}
```

#### 주문 조회
```http
GET /api/orders?symbol=BTC/USDT&status=PENDING
Authorization: Bearer {token}
```

#### 주문 취소
```http
DELETE /api/orders/{orderId}
Authorization: Bearer {token}
```

### 입출금

#### 출금 요청
```http
POST /api/wallets/withdrawal
Authorization: Bearer {token}
Content-Type: application/json

{
  "asset": "BTC",
  "amount": "0.5",
  "toAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
}
```

---

## 🔒 보안

### 구현된 보안 기능

1. **인증/인가**
   - JWT 기반 인증
   - Spring Security
   - API Key 인증

2. **Rate Limiting**
   - Redis 기반 분산 Rate Limiting
   - IP/API Key 별 제한

3. **데이터 보호**
   - 비밀번호 BCrypt 암호화
   - API Secret Key 암호화
   - HTTPS 강제 (프로덕션)

4. **동시성 제어**
   - 비관적 락 (Pessimistic Lock)
   - 낙관적 락 (Optimistic Lock)
   - 트랜잭션 격리 수준 관리

---

## ⚡ 성능

### 최적화 기법

1. **캐싱**
   - Redis L2 캐시
   - 계층별 TTL 설정

2. **데이터베이스**
   - 인덱스 최적화
   - Connection Pool (HikariCP)
   - 배치 처리

3. **스케일링**
   - Horizontal Pod Autoscaler (HPA)
   - 3-10 pods 자동 스케일링
   - CPU 70%, Memory 80% 기준

---

## 📊 모니터링

### Prometheus Metrics

- **거래 메트릭**: 거래량, 수수료 수익
- **시스템 메트릭**: CPU, 메모리, 디스크
- **애플리케이션 메트릭**: API 응답 시간, 에러율

### Grafana 대시보드

```bash
# Grafana 접속
http://localhost:3000

# 기본 계정
Username: admin
Password: admin
```

---

## 📄 라이선스

MIT License

---

## 👥 기여자

- **DongKyeong** - Initial work

---

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 등록해주세요.

---

## 🗺 로드맵

### ✅ 완료된 기능
- [x] 기본 거래 시스템
- [x] 입출금 시스템
- [x] 수수료 시스템
- [x] API Key 관리
- [x] Rate Limiting
- [x] Redis 캐싱
- [x] Docker & Kubernetes
- [x] 모니터링 (Prometheus/Grafana)

### 🚧 진행중
- [ ] 고급 주문 타입 (OCO, Trailing Stop)
- [ ] 마진/레버리지 거래
- [ ] KYC/AML 시스템

### 📅 계획중
- [ ] 스테이킹 시스템
- [ ] P2P 거래
- [ ] 소셜 트레이딩 (Copy Trading)
- [ ] 모바일 앱 (React Native)

---

**⭐ 프로젝트가 도움이 되셨다면 Star를 눌러주세요!**
