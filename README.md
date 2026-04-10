# OHT/AGV 예지보전 모니터링 시스템

반도체 FAB의 OHT/AGV 이송장치 센서 데이터를 실시간 모니터링하고, 규칙 기반 이상 감지 및 정비 오더 관리까지 연결하는 설비 예지보전 웹 시스템입니다.

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)
![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?logo=vuedotjs)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Java 17, Spring Boot 3.x, MyBatis, MySQL 8.0, Maven |
| **Frontend** | Vue.js 3 (Composition API), Vite, Pinia, Vue Router, ECharts |
| **실시간** | WebSocket (STOMP + SockJS), Spring Scheduler |
| **인증** | JWT (jjwt 0.12.x), BCrypt |
| **리포트** | Apache POI (엑셀 생성) |
| **배포** | Docker, docker-compose, Nginx |

---

## 주요 기능

### MVP (핵심 모니터링)
- 대시보드: 장비 40대 상태 카운트 + 카드 그리드 + 알림 피드
- 장비 상세: 실시간 센서 차트 4종 (먼지/온도/전류/열화상) WebSocket Push
- 데이터 리플레이: 8,000건 센서 데이터를 2초 간격 순환 재생
- 이상 감지: 임계값 기반 RuleEngine, CAUTION/WARNING/DANGER 3단계
- 알림: 실시간 토스트 + 이력 테이블 + 확인 처리

### V1.1 (운영)
- 정비 오더: OPEN → ASSIGNED → IN_PROGRESS → COMPLETED 상태 전이
- 장비 자동 복귀: 정비 완료 시 RUNNING 상태로 자동 변경
- 임계값 관리: ADMIN 인라인 편집 + 캐시 자동 갱신
- 로그인/JWT: BCrypt 인증, 24시간 토큰, 라우트 가드

### V1.2 (분석)
- 통계 대시보드: 상태 분포 도넛, 위험 랭킹 바, 건강 점수 게이지, 센서 추이 라인
- 건강 점수: 센서 이탈비율 가중 합산 (0~100점)
- 엑셀 리포트: 센서 평균 / 위험 랭킹 / 건강 점수 3시트
- Docker 컨테이너화

---

## 시스템 아키텍처

```
┌─────────────┐     WebSocket(STOMP)     ┌──────────────────┐
│             │ ◄──────────────────────── │                  │
│   Vue.js    │         REST API         │  Spring Boot     │
│  (Nginx)    │ ──────────────────────► │   Backend        │
│   :80       │                          │   :8080          │
└─────────────┘                          └────────┬─────────┘
                                                  │
                                         ┌────────▼─────────┐
                                         │  MySQL 8.0       │
                                         │  oht_monitor     │
                                         │  :3306           │
                                         └──────────────────┘

[Scheduler 2초] ─► sensor_reading 순차 로드
                 ─► /topic/sensor/{eqId} Push
                 ─► RuleEngine.evaluate()
                 ─► 이상 감지 시 /topic/alert Push + alert_event INSERT
```

---

## 화면 구성

| ID | 화면 | 경로 | 설명 |
|----|------|------|------|
| SC-01 | 메인 대시보드 | `/dashboard` | 상태 카운트 + 장비 카드 + 알림 피드 |
| SC-02 | 장비 상세 | `/dashboard/:eqId` | 실시간 ECharts 4종 + 장비 정보 |
| SC-03 | 장비 목록 | `/equipment` | 유형/상태 필터 + 테이블 |
| SC-04 | 알림 이력 | `/alerts` | 등급/장비 필터 + 확인 처리 |
| SC-05 | 로그인 | `/login` | ID/PW 입력 + JWT 발급 |
| SC-06 | 정비 오더 목록 | `/maintenance` | 상태 필터 + 생성 모달 |
| SC-07 | 정비 오더 상세 | `/maintenance/:id` | 상태 스텝퍼 + 전이 액션 |
| SC-08 | 임계값 설정 | `/admin/thresholds` | OHT/AGV 탭 + 인라인 편집 |
| SC-09 | 통계 & 리포트 | `/stats` | 차트 4종 + 엑셀 다운로드 |

---

## 실행 방법

### Docker (권장)

```bash
docker-compose up -d
```

- 프론트엔드: http://localhost
- 백엔드 API: http://localhost:8080
- MySQL: localhost:3306

### 로컬 개발

**사전 요구사항**: Java 17, Node.js 20+, MySQL 8.0

```bash
# 1. MySQL에 oht_monitor 데이터베이스 생성
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS oht_monitor"

# 2. Backend 실행
cd back
./mvnw spring-boot:run

# 3. Frontend 실행 (별도 터미널)
cd front
npm install
npm run dev
```

- 프론트엔드: http://localhost:5173
- 백엔드 API: http://localhost:8080

### 테스트 계정

| 사용자 | 비밀번호 | 역할 |
|--------|----------|------|
| admin | admin1234 | ADMIN |
| engineer1 | eng1234 | ENGINEER |
| engineer2 | eng1234 | ENGINEER |

---

## API 목록

| Method | Path | 설명 | Phase |
|--------|------|------|-------|
| GET | `/api/v1/equipment` | 장비 목록 (필터: type, status) | MVP |
| GET | `/api/v1/equipment/{eqId}` | 장비 상세 | MVP |
| POST | `/api/v1/equipment` | 장비 등록 | MVP |
| PATCH | `/api/v1/equipment/{eqId}/status` | 장비 상태 변경 | V1.1 |
| GET | `/api/v1/sensor/{eqId}/latest` | 센서 최신값 | MVP |
| GET | `/api/v1/sensor/{eqId}/history` | 센서 이력 | MVP |
| GET | `/api/v1/sensor/summary` | 상태 요약 | MVP |
| GET | `/api/v1/alerts` | 알림 목록 (필터 + 페이지네이션) | MVP |
| PATCH | `/api/v1/alerts/{id}/acknowledge` | 알림 확인 | MVP |
| GET | `/api/v1/threshold` | 임계값 목록 | V1.1 |
| PUT | `/api/v1/threshold/{id}` | 임계값 수정 | V1.1 |
| GET | `/api/v1/maintenance` | 정비 오더 목록 | V1.1 |
| POST | `/api/v1/maintenance` | 정비 오더 생성 | V1.1 |
| GET | `/api/v1/maintenance/{id}` | 오더 상세 | V1.1 |
| PATCH | `/api/v1/maintenance/{id}/status` | 오더 상태 전이 | V1.1 |
| POST | `/api/v1/auth/login` | 로그인 (JWT 발급) | V1.1 |
| GET | `/api/v1/stats/sensor-avg` | 센서 평균/최대 | V1.2 |
| GET | `/api/v1/stats/state-distribution` | 상태 분포 | V1.2 |
| GET | `/api/v1/stats/danger-ranking` | 위험 랭킹 | V1.2 |
| GET | `/api/v1/stats/health-score` | 건강 점수 | V1.2 |
| GET | `/api/v1/stats/sensor-trend` | 센서 추이 | V1.2 |
| GET | `/api/v1/stats/export` | 엑셀 리포트 | V1.2 |

---

## 프로젝트 구조

```
oht_monitoring/
├── docker-compose.yml
├── README.md
├── back/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/ohtmon/
│       │   ├── OhtmonApplication.java
│       │   ├── common/          # ApiResponse, GlobalExceptionHandler
│       │   ├── config/          # WebSocket, CORS, JWT, 데이터 초기화
│       │   ├── controller/      # Equipment, Sensor, Alert, MaintOrder, Stats, Auth, Threshold
│       │   ├── service/         # 비즈니스 로직, RuleEngine, ExcelExport
│       │   ├── mapper/          # MyBatis 매퍼 인터페이스
│       │   ├── dto/             # 요청/응답 DTO
│       │   ├── domain/          # 엔티티 (Equipment, SensorReading, AlertEvent, etc.)
│       │   └── scheduler/       # DataReplayScheduler
│       └── resources/
│           ├── mapper/          # MyBatis XML
│           ├── schema.sql       # DDL
│           ├── data.sql         # 초기 데이터
│           ├── sensor-data.sql  # 센서 더미 데이터 8,000건
│           └── application.yml
└── front/
    ├── Dockerfile
    ├── nginx.conf
    ├── package.json
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/             # Axios 인스턴스
        ├── router/          # Vue Router + 인증 가드
        ├── composables/     # useWebSocket, useAlertToast
        ├── components/      # StatusBadge, AppSidebar, AlertToast
        └── views/           # 9개 페이지 컴포넌트
```

---

## ERD

```
┌──────────────┐     ┌───────────────────┐     ┌───────────────┐
│  equipment   │     │  sensor_reading   │     │ threshold_rule│
├──────────────┤     ├───────────────────┤     ├───────────────┤
│ eq_id (PK)   │◄────│ eq_id (FK)        │     │ id (PK)       │
│ eq_name      │     │ id (PK)           │     │ eq_type       │
│ eq_type      │     │ collected_at      │     │ sensor_name   │
│ manufacturer │     │ pm10, pm25        │     │ caution_value │
│ location     │     │ ntc_temp          │     │ warning_value │
│ install_date │     │ ct1~ct4           │     │ danger_value  │
│ status       │     │ ir_temp_max       │     └───────────────┘
│ created_at   │     │ state (0~3)       │
└──────┬───────┘     └───────────────────┘
       │
       │         ┌───────────────┐     ┌────────────────┐
       │         │  alert_event  │     │  maint_order   │
       │         ├───────────────┤     ├────────────────┤
       ├────────►│ eq_id (FK)    │◄────│ alert_event_id │
       │         │ id (PK)       │     │ id (PK)        │
       │         │ alert_level   │     │ eq_id (FK)     │
       │         │ sensor_name   │     │ order_type     │
       │         │ sensor_value  │     │ priority       │
       │         │ acknowledged  │     │ status         │
       │         └───────────────┘     │ assignee       │
       │                               │ action_taken   │
       │         ┌───────────────┐     └────────────────┘
       │         │    user       │
       │         ├───────────────┤
       │         │ id (PK)       │
       │         │ username (UQ) │
       │         │ password      │
       │         │ name          │
       │         │ role          │
       │         └───────────────┘
```

---

## 모듈 의존성

```
장비(M1) ← 센서(M2) ← 알림(M3) ← 정비(M4) ← 통계(M5)
                                                  ↑
                                              인증(M6)
```

오른쪽 모듈이 없어도 왼쪽 모듈은 정상 동작합니다.

---

## 개발 기간

| 단계 | 기간 | 내용 |
|------|------|------|
| MVP | Day 1~2 | 대시보드, 실시간 차트, 이상 감지, 알림 |
| V1.1 | Day 3~4 | 정비 오더, 로그인/JWT, 임계값 관리 |
| V1.2 | Day 5 | 통계 차트, 엑셀 리포트, Docker, README |
