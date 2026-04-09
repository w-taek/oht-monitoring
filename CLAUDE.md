# CLAUDE.md — OHT/AGV 예지보전 모니터링 시스템

## 프로젝트 개요
OHT/AGV 이송장치의 센서 데이터(먼지, 온도, 전류, 열화상)를 실시간 모니터링하고,
규칙 기반으로 이상을 감지하여 알림 → 정비 오더까지 추적하는 설비 예지보전 웹 시스템.

## 기술스택
- **Backend**: Java 17, Spring Boot 3.x, MyBatis, MySQL 8.0 (Maven 빌드)
- **Frontend**: Vue.js 3 (Composition API + `<script setup>`), Vite, Pinia, Vue Router, ECharts (vue-echarts)
- **실시간**: WebSocket (STOMP + SockJS), Spring Scheduler
- **인증**: JWT (jjwt 0.12.x), BCrypt 비밀번호 해시
- **리포트**: Apache POI (엑셀 생성)
- **배포**: Docker + docker-compose

---

## 릴리즈 전략 (MVP → V1.1 → V1.2)

### MVP (Day 1~2) — 최소 시연 가능 제품
대시보드 → 실시간 차트 → 이상 감지 → 알림 Push
- FR-101~103: 장비 CRUD (목록/상세/등록)
- FR-201~203: 대시보드, 상태 요약, 실시간 차트
- FR-205~207: 센서 이력, WebSocket, 데이터 리플레이
- FR-301, 303, 304: 이상 판별, 알림 저장, 알림 Push
- FR-306, 307: 알림 목록 조회, 알림 확인

### V1.1 (Day 3~4) — 운영 기능
알림→정비→복구 업무 루프 + 로그인/권한
- FR-105: 장비 상태 변경
- FR-305: 중복 알림 억제
- FR-308: 임계값 관리
- FR-401, 402, 404~406: 정비 오더 CRUD + 상태 전이 + 장비 복귀
- FR-601, 602: 로그인/JWT + 역할 기반 접근제어

### V1.2 (Day 5) — 분석 & 마감
통계 차트 + 엑셀 리포트 + Docker + README
- FR-501~503: 통계 차트, MTBF/MTTR, 건강 점수
- FR-506: 엑셀 리포트
- NFR-12: Docker 컨테이너화
- DOC-01: README + 데모 준비

### 핵심 원칙
V1.1, V1.2는 MVP 위에 **얹는(additive)** 구조.
모듈 단방향 의존: 장비(M1) ← 센서(M2) ← 알림(M3) ← 정비(M4) ← 통계(M5)
오른쪽 모듈이 없어도 왼쪽 모듈은 정상 동작해야 함.

---

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
│   ├── components/      # 공통 컴포넌트 (StatusBadge, DataTable, Toast 등)
│   ├── composables/     # useWebSocket 등
│   ├── views/           # 페이지 컴포넌트
│   ├── stores/          # Pinia 스토어
│   ├── router/          # Vue Router
│   └── assets/
```

---

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
    caution_value DOUBLE NOT NULL,            -- 관심(🔵) 임계값
    warning_value DOUBLE NOT NULL,            -- 경고(🟡) 임계값
    danger_value DOUBLE NOT NULL,             -- 위험(🔴) 임계값
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

---

## 임계값 초기 데이터 기준

AI Hub 실제 데이터 분포를 참고한 초기값:

| 센서 | 단위 | 관심(CAUTION) | 경고(WARNING) | 위험(DANGER) |
|------|------|---------------|---------------|--------------|
| pm10 | µg/m³ | 30 | 50 | 80 |
| pm25 | µg/m³ | 15 | 25 | 40 |
| ntc_temp | ℃ | 40 | 55 | 70 |
| ct1 | A | 3.0 | 5.0 | 8.0 |
| ct2 | A | 3.0 | 5.0 | 8.0 |
| ct3 | A | 3.0 | 5.0 | 8.0 |
| ct4 | A | 3.0 | 5.0 | 8.0 |
| ir_temp_max | ℃ | 45 | 65 | 85 |

OHT와 AGV 각각 위 8종 = 총 16행.
(OHT와 AGV는 동일값으로 시작하되, 나중에 ADMIN이 조정 가능)

---

## 초기 사용자 데이터

| username | password (평문→BCrypt) | name | role |
|----------|----------------------|------|------|
| admin | admin1234 | 관리자 | ADMIN |
| engineer1 | eng1234 | 김엔지니어 | ENGINEER |
| engineer2 | eng1234 | 이엔지니어 | ENGINEER |

data.sql에는 BCrypt 해시된 비밀번호를 넣어야 함.

---

## 장비 초기 데이터 규칙

총 40대:
- OHT-01 ~ OHT-20: 제조사 A사(7대), B사(7대), C사(6대)
- AGV-01 ~ AGV-20: 제조사 D사(10대), E사(10대)

위치: FAB-A Line1, FAB-A Line2, FAB-B Line1, FAB-B Line2, FAB-C Line1 등으로 분산.
설치일: 2020~2024년 사이 랜덤.

---

## API 설계 규칙
- 모든 응답은 `ApiResponse<T>` 래핑: `{ status, data, message, timestamp }`
- REST 경로 규칙: `/api/v1/{resource}`
- 페이지네이션: `?page=0&size=20`
- 에러: GlobalExceptionHandler에서 일괄 처리
- 성공 status: `"SUCCESS"`, 실패 status: `"FAIL"`

## 핵심 API 목록

| Method | Path | 설명 | Module | Phase |
|--------|------|------|--------|-------|
| GET | /api/v1/equipment | 장비 목록 (필터: type, status) | M1 | MVP |
| GET | /api/v1/equipment/{eqId} | 장비 상세 | M1 | MVP |
| POST | /api/v1/equipment | 장비 등록 | M1 | MVP |
| PATCH | /api/v1/equipment/{eqId}/status | 장비 상태 변경 | M1 | V1.1 |
| GET | /api/v1/sensor/{eqId}/latest | 센서 최신값 | M2 | MVP |
| GET | /api/v1/sensor/{eqId}/history | 센서 이력 (기간 필터) | M2 | MVP |
| GET | /api/v1/sensor/summary | 전체 장비 상태 요약 | M2 | MVP |
| GET | /api/v1/alerts | 알림 목록 (필터: level, eqId) | M3 | MVP |
| PATCH | /api/v1/alerts/{id}/acknowledge | 알림 확인 처리 | M3 | MVP |
| GET | /api/v1/threshold | 임계값 목록 | M3 | V1.1 |
| PUT | /api/v1/threshold/{id} | 임계값 수정 (ADMIN만) | M3 | V1.1 |
| GET | /api/v1/maintenance | 정비 오더 목록 | M4 | V1.1 |
| POST | /api/v1/maintenance | 정비 오더 생성 | M4 | V1.1 |
| GET | /api/v1/maintenance/{id} | 오더 상세 | M4 | V1.1 |
| PATCH | /api/v1/maintenance/{id}/status | 오더 상태 전이 | M4 | V1.1 |
| GET | /api/v1/stats/sensor-avg | 통계: 센서 평균/최대 | M5 | V1.2 |
| GET | /api/v1/stats/state-distribution | 통계: 상태 분포 | M5 | V1.2 |
| GET | /api/v1/stats/danger-ranking | 통계: 위험 랭킹 | M5 | V1.2 |
| GET | /api/v1/stats/health-score | 통계: 건강 점수 | M5 | V1.2 |
| GET | /api/v1/stats/export | 엑셀 다운로드 | M5 | V1.2 |
| POST | /api/v1/auth/login | 로그인 → JWT | M6 | V1.1 |

---

## WebSocket 설정

- STOMP endpoint: `/ws` (SockJS fallback)
- 센서 데이터 토픽: `/topic/sensor/{eqId}` — 1~2초 간격 Push
- 알림 토픽: `/topic/alert` — 이상 감지 시 Push
- 자동 재접속: 3회 재시도

### 센서 데이터 Push 메시지 포맷
```json
{
  "eqId": "OHT-01",
  "pm10": 42.5,
  "pm25": 18.3,
  "ntcTemp": 38.2,
  "ct1": 2.1,
  "ct2": 1.8,
  "ct3": 2.3,
  "ct4": 1.9,
  "irTempMax": 45.6,
  "exTemp": 23.5,
  "exHumidity": 45.0,
  "state": 1,
  "collectedAt": "2024-01-15T10:30:00"
}
```

### 알림 Push 메시지 포맷
```json
{
  "alertId": 123,
  "eqId": "OHT-03",
  "alertLevel": "WARNING",
  "sensorName": "pm10",
  "sensorValue": 52.3,
  "thresholdValue": 50.0,
  "createdAt": "2024-01-15T10:30:05"
}
```

---

## 비즈니스 로직 상세

### 이상감지 (RuleEngine)
- 매 센서 데이터 수신마다 threshold_rule과 비교
- 해당 장비유형(OHT/AGV)의 임계값을 조회
- 센서값이 가장 높은 등급의 임계값을 초과하면 해당 등급으로 판별
- 판별 결과가 CAUTION 이상이면 alert_event 생성 + WebSocket Push

### 중복 알림 억제 (FR-305, V1.1)
- 같은 장비(eq_id) + 같은 센서(sensor_name)에 대해 5분 내 중복 알림 방지
- 최근 알림의 created_at을 체크하여 5분 이내면 새 알림 생성 안 함

### 데이터 리플레이 (Scheduler)
- Spring @Scheduled(fixedRate=2000) — 2초 간격
- 장비별로 sensor_reading 테이블에서 순차 로드 (인덱스 관리)
- 끝나면 처음부터 반복 (circular)
- 각 데이터 포인트마다 RuleEngine 호출 → 임계 초과 시 AlertService 호출

### 정비 오더 상태 전이
```
OPEN → ASSIGNED → IN_PROGRESS → COMPLETED
```
- OPEN → ASSIGNED: assignee(담당자) 필수 입력
- ASSIGNED → IN_PROGRESS: started_at 자동 기록
- IN_PROGRESS → COMPLETED: action_taken(조치 내용) 필수 입력, completed_at 자동 기록
- COMPLETED 시: 해당 장비 status를 자동으로 'RUNNING'으로 복귀

### 건강 점수 (Health Score) 계산식 (V1.2)
```
점수 = 100 - (pm10_이탈비율×0.3 + ntc_이탈비율×0.3 + ct_이탈비율×0.2 + ir_이탈비율×0.2) × 100
```
- 이탈비율 = 해당 센서가 caution 임계값을 초과한 횟수 / 전체 측정 횟수
- 범위: 0~100 (높을수록 건강)

---

## 권한 매트릭스 (V1.1 기준)

| 기능 | 비로그인 | USER(ENGINEER) | ADMIN |
|------|----------|----------------|-------|
| 대시보드/장비/센서 조회 | ❌ | ✅ | ✅ |
| 알림 목록/확인 | ❌ | ✅ | ✅ |
| 정비 오더 CRUD | ❌ | ✅ | ✅ |
| 통계/리포트 조회 | ❌ | ✅ | ✅ |
| 장비 등록/수정 | ❌ | ❌ | ✅ |
| 임계값 설정 | ❌ | ❌ | ✅ |

MVP에서는 로그인 없이 전체 접근 허용. 인증은 V1.1에서 추가.

---

## 화면 요구사항

| ID | 화면명 | 경로 | Phase | 주요 구성 요소 |
|----|--------|------|-------|---------------|
| SC-01 | 메인 대시보드 | `/dashboard` | MVP | 상태 카운트(4) + 장비 카드 그리드 + 알림 피드 |
| SC-02 | 장비 상세 모니터링 | `/dashboard/:eqId` | MVP | 장비 정보 + 실시간 센서 차트(4종) + 알림 히스토리 |
| SC-03 | 장비 목록 | `/equipment` | MVP | 필터(유형/상태) + 테이블 + 등록 버튼 |
| SC-04 | 알림 이력 | `/alerts` | MVP | 알림 테이블 + 확인 버튼 + 등급/장비 필터 |
| SC-05 | 로그인 | `/login` | V1.1 | ID/PW 입력 + 로그인 버튼 |
| SC-06 | 정비 오더 목록 | `/maintenance` | V1.1 | 상태 필터 + 테이블 + 생성 버튼 |
| SC-07 | 정비 오더 상세 | `/maintenance/:id` | V1.1 | 폼 + 상태 전이 버튼 + 조치 내용 입력 |
| SC-08 | 임계값 설정 | `/admin/thresholds` | V1.1 | 센서별 임계값 테이블 + 인라인 편집 (ADMIN만) |
| SC-09 | 통계 & 리포트 | `/stats` | V1.2 | 차트 5~7종 + 엑셀 다운로드 버튼 |

---

## 상태 색상 규칙 (전 화면 통일)
- 🟢 정상 (state=0): `#059669` / green
- 🔵 관심 (state=1): `#2563eb` / blue  
- 🟡 경고 (state=2): `#d97706` / yellow
- 🔴 위험 (state=3): `#dc2626` / red

---

## 통계 파생변수 (V1.2)

### 원천 데이터 직접 집계 (1차 파생)
- 장비별 센서 평균/최대/최소: `GROUP BY eq_id + AVG/MAX/MIN`
- 상태 분포 카운트: `GROUP BY state + COUNT`
- 시간대별 센서 평균: `GROUP BY HOUR(collected_at)`
- OHT vs AGV 그룹 비교: `JOIN equipment + GROUP BY eq_type`

### 비율/조합 계산 (2차 파생)
- 장비별 위험 비율: `SUM(state=3)/COUNT(*)*100`
- 위험 상태 간 평균 간격 (MTBF 대체): `state=3인 행의 collected_at 차이 평균`

### 복합 계산 (3차 파생)
- 건강 점수: 위 계산식 참조
- 위험 비율 기준 장비 랭킹: `위험 비율 DESC 정렬 → TOP N`

### 더미 데이터 필요
- MTTR: `maint_order.(completed_at - created_at) 평균` — 더미 정비 이력 20~30건 필요
- 정비 유형별 분포: `maint_order.order_type GROUP BY`

---

## 빌드 & 실행
```bash
# Backend (Maven)
cd back && ./mvnw spring-boot:run

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

## 작업 범위 제한
- 이 프로젝트 루트 내부 파일만 생성/수정할 것
- back/ 과 front/ 디렉토리 안에서만 작업
- 프로젝트 외부 경로에 파일을 생성하거나 접근하지 말 것
