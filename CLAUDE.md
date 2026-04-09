# CLAUDE.md — OHT/AGV 예지보전 모니터링 시스템

## 프로젝트 개요
OHT/AGV 이송장치의 센서 데이터(먼지, 온도, 전류, 열화상)를 실시간 모니터링하고,
규칙 기반으로 이상을 감지하여 알림 → 정비 오더까지 추적하는 설비 예지보전 웹 시스템.

## 기술스택
- **Backend**: Java 17, Spring Boot 3.x, MyBatis, MySQL 8.0
- **Frontend**: Vue.js 3 (Composition API), Vite, Pinia, Vue Router, ECharts (vue-echarts)
- **실시간**: WebSocket (STOMP + SockJS), Spring Scheduler
- **인증**: JWT (jjwt 0.12.x)
- **배포**: Docker + docker-compose

## 프로젝트 구조
```
back/
├── src/main/java/com/example/ohtmon/
│   ├── config/          # WebSocket, CORS, Security 설정
│   ├── controller/      # REST API 컨트롤러
│   ├── service/         # 비즈니스 로직
│   ├── mapper/          # MyBatis 매퍼 인터페이스
│   ├── dto/             # 요청/응답 DTO
│   ├── domain/          # 도메인 엔티티
│   ├── common/          # ApiResponse, GlobalExceptionHandler
│   └── scheduler/       # 데이터 리플레이 Scheduler
├── src/main/resources/
│   ├── mapper/          # MyBatis XML 매퍼
│   ├── schema.sql       # DDL
│   ├── data.sql         # 초기 데이터
│   └── application.yml
front/
├── src/
│   ├── api/             # Axios 인스턴스 + API 모듈
│   ├── components/      # 공통 컴포넌트
│   ├── composables/     # useWebSocket 등
│   ├── views/           # 페이지 컴포넌트
│   ├── stores/          # Pinia 스토어
│   ├── router/          # Vue Router
│   └── assets/
```

## DB 스키마 (핵심 테이블)
```sql
-- 장비
CREATE TABLE equipment (
    eq_id VARCHAR(20) PRIMARY KEY,           -- 'OHT-01', 'AGV-01'
    eq_name VARCHAR(100) NOT NULL,
    eq_type ENUM('OHT','AGV') NOT NULL,
    manufacturer VARCHAR(50),
    location VARCHAR(100),
    install_date DATE,
    status ENUM('RUNNING','STOPPED','MAINTENANCE') DEFAULT 'RUNNING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 센서 데이터 (AI Hub 원천 데이터 적재)
CREATE TABLE sensor_reading (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eq_id VARCHAR(20) NOT NULL,
    collected_at TIMESTAMP NOT NULL,
    pm10 DOUBLE, pm25 DOUBLE, pm10_val DOUBLE,
    ntc_temp DOUBLE,
    ct1 DOUBLE, ct2 DOUBLE, ct3 DOUBLE, ct4 DOUBLE,
    ir_temp_max DOUBLE, ir_x INT, ir_y INT,
    ex_temp DOUBLE, ex_humidity DOUBLE, ex_lux DOUBLE,
    state TINYINT DEFAULT 0,                  -- 0=정상, 1=관심, 2=경고, 3=위험
    cumulative_operating_day INT,
    equipment_history INT,
    FOREIGN KEY (eq_id) REFERENCES equipment(eq_id),
    INDEX idx_eq_collected (eq_id, collected_at),
    INDEX idx_state (state)
);

-- 임계값 규칙
CREATE TABLE threshold_rule (
    id INT AUTO_INCREMENT PRIMARY KEY,
    eq_type ENUM('OHT','AGV') NOT NULL,
    sensor_name VARCHAR(30) NOT NULL,         -- 'pm10', 'ntc_temp', 'ct1', 'ir_temp_max'
    caution_value DOUBLE NOT NULL,            -- 관심 임계값
    warning_value DOUBLE NOT NULL,            -- 경고 임계값
    danger_value DOUBLE NOT NULL,             -- 위험 임계값
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 알림 이벤트
CREATE TABLE alert_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eq_id VARCHAR(20) NOT NULL,
    alert_level ENUM('CAUTION','WARNING','DANGER') NOT NULL,
    sensor_name VARCHAR(30) NOT NULL,
    sensor_value DOUBLE NOT NULL,
    threshold_value DOUBLE NOT NULL,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (eq_id) REFERENCES equipment(eq_id),
    INDEX idx_eq_level (eq_id, alert_level),
    INDEX idx_created (created_at DESC)
);

-- 정비 오더
CREATE TABLE maint_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eq_id VARCHAR(20) NOT NULL,
    alert_event_id BIGINT NULL,
    order_type ENUM('PREVENTIVE','CORRECTIVE','EMERGENCY') NOT NULL,
    priority ENUM('LOW','MEDIUM','HIGH','CRITICAL') DEFAULT 'MEDIUM',
    status ENUM('OPEN','ASSIGNED','IN_PROGRESS','COMPLETED') DEFAULT 'OPEN',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    assignee VARCHAR(50),
    action_taken TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (eq_id) REFERENCES equipment(eq_id),
    FOREIGN KEY (alert_event_id) REFERENCES alert_event(id)
);

-- 사용자
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(200) NOT NULL,           -- BCrypt 해시
    name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN','ENGINEER','OPERATOR','VIEWER') DEFAULT 'ENGINEER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## API 설계 규칙
- 모든 응답은 `ApiResponse<T>` 래핑: `{ status, data, message, timestamp }`
- REST 경로 규칙: `/api/v1/{resource}`
- 페이지네이션: `?page=0&size=20`
- 에러: GlobalExceptionHandler에서 일괄 처리

## 핵심 API 목록
| Method | Path | 설명 | Module |
|--------|------|------|--------|
| GET | /api/v1/equipment | 장비 목록 (필터: type, status) | M1 |
| GET | /api/v1/equipment/{eqId} | 장비 상세 | M1 |
| POST | /api/v1/equipment | 장비 등록 | M1 |
| PATCH | /api/v1/equipment/{eqId}/status | 장비 상태 변경 | M1 |
| GET | /api/v1/sensor/{eqId}/latest | 센서 최신값 | M2 |
| GET | /api/v1/sensor/{eqId}/history | 센서 이력 (기간 필터) | M2 |
| GET | /api/v1/sensor/summary | 전체 장비 상태 요약 | M2 |
| GET | /api/v1/alerts | 알림 목록 (필터: level, eqId) | M3 |
| PATCH | /api/v1/alerts/{id}/acknowledge | 알림 확인 처리 | M3 |
| GET | /api/v1/threshold | 임계값 목록 | M3 |
| PUT | /api/v1/threshold/{id} | 임계값 수정 | M3 |
| GET | /api/v1/maintenance | 정비 오더 목록 | M4 |
| POST | /api/v1/maintenance | 정비 오더 생성 | M4 |
| GET | /api/v1/maintenance/{id} | 오더 상세 | M4 |
| PATCH | /api/v1/maintenance/{id}/status | 오더 상태 전이 | M4 |
| GET | /api/v1/stats/sensor-avg | 통계: 센서 평균/최대 | M5 |
| GET | /api/v1/stats/state-distribution | 통계: 상태 분포 | M5 |
| GET | /api/v1/stats/danger-ranking | 통계: 위험 랭킹 | M5 |
| GET | /api/v1/stats/health-score | 통계: 건강 점수 | M5 |
| GET | /api/v1/stats/export | 엑셀 다운로드 | M5 |
| POST | /api/v1/auth/login | 로그인 → JWT | M6 |

## WebSocket 엔드포인트
- STOMP endpoint: `/ws`
- 센서 데이터: `/topic/sensor/{eqId}` — 1~2초 간격 Push
- 알림: `/topic/alert` — 이상 감지 시 Push

## 상태 색상 규칙 (전 화면 통일)
- 🟢 정상 (state=0): `#059669` / green
- 🔵 관심 (state=1): `#2563eb` / blue  
- 🟡 경고 (state=2): `#d97706` / yellow
- 🔴 위험 (state=3): `#dc2626` / red

## 정비 오더 상태 전이
OPEN → ASSIGNED (담당자 입력) → IN_PROGRESS (작업 시작) → COMPLETED (조치 완료)
COMPLETED 시 해당 장비 status를 자동으로 'RUNNING'으로 복귀.

## 빌드 & 실행
```bash
# Backend
cd back && ./gradlew bootRun

# Frontend
cd front && npm install && npm run dev

# Docker
docker-compose up -d
```

## 코드 컨벤션
- Git: Conventional Commits (feat/fix/chore/refactor)
- Java: IntelliJ 기본 포맷터, 4 space indent
- Vue: ESLint + Prettier, Composition API + `<script setup>`
- MyBatis XML: mapper/ 디렉토리, 네임스페이스 = 매퍼 인터페이스 FQCN
