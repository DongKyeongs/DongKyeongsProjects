# Docker 실행 가이드

## 필수 요구사항
- Docker
- Docker Compose

## 실행 방법

### 1. 전체 스택 실행 (권장)
```bash
docker-compose up -d
```

### 2. 개별 서비스 실행
```bash
# MySQL만 실행
docker-compose up -d mysql

# 백엔드만 실행 (MySQL, Redis 의존)
docker-compose up -d backend

# 프론트엔드만 실행
docker-compose up -d frontend
```

### 3. 로그 확인
```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
```

### 4. 중지 및 제거
```bash
# 중지
docker-compose stop

# 중지 및 제거
docker-compose down

# 볼륨 포함 모든 것 제거
docker-compose down -v
```

## 서비스 포트
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- MySQL: localhost:3306
- Redis: localhost:6379

## 환경 변수
`docker-compose.yml` 파일에서 환경 변수를 수정할 수 있습니다:
- `JWT_SECRET`: JWT 시크릿 키 (프로덕션에서는 반드시 변경)
- `MYSQL_ROOT_PASSWORD`: MySQL root 비밀번호
- `MYSQL_DATABASE`: 데이터베이스 이름

## 데이터 백업
```bash
# MySQL 데이터 백업
docker exec exchange-mysql mysqldump -u root -proot exchange > backup.sql

# 데이터 복원
docker exec -i exchange-mysql mysql -u root -proot exchange < backup.sql
```

## 개발 모드
개발 중에는 docker-compose를 사용하지 않고 로컬에서 실행하는 것을 권장합니다:
```bash
# MySQL과 Redis만 Docker로 실행
docker-compose up -d mysql redis

# 백엔드와 프론트엔드는 로컬에서 실행
./gradlew bootRun
cd frontend && npm start
```
