-- =============================================
-- OHT/AGV 예지보전 모니터링 시스템 초기 데이터
-- =============================================

-- -----------------------------------------------
-- 1. 장비 40대 (OHT-01~20, AGV-01~20)
-- -----------------------------------------------
INSERT IGNORE INTO equipment (eq_id, eq_name, eq_type, manufacturer, location, install_date, status) VALUES
-- OHT 20대 (제조사: A사, B사, C사)
('OHT-01', 'OHT 1호기', 'OHT', 'A사', 'FAB-A Line1', '2023-01-15', 'RUNNING'),
('OHT-02', 'OHT 2호기', 'OHT', 'A사', 'FAB-A Line1', '2023-01-15', 'RUNNING'),
('OHT-03', 'OHT 3호기', 'OHT', 'A사', 'FAB-A Line2', '2023-02-10', 'RUNNING'),
('OHT-04', 'OHT 4호기', 'OHT', 'B사', 'FAB-A Line2', '2023-02-10', 'RUNNING'),
('OHT-05', 'OHT 5호기', 'OHT', 'B사', 'FAB-A Line3', '2023-03-20', 'RUNNING'),
('OHT-06', 'OHT 6호기', 'OHT', 'B사', 'FAB-A Line3', '2023-03-20', 'STOPPED'),
('OHT-07', 'OHT 7호기', 'OHT', 'C사', 'FAB-B Line1', '2023-04-05', 'RUNNING'),
('OHT-08', 'OHT 8호기', 'OHT', 'C사', 'FAB-B Line1', '2023-04-05', 'RUNNING'),
('OHT-09', 'OHT 9호기', 'OHT', 'C사', 'FAB-B Line2', '2023-05-12', 'MAINTENANCE'),
('OHT-10', 'OHT 10호기', 'OHT', 'A사', 'FAB-B Line2', '2023-05-12', 'RUNNING'),
('OHT-11', 'OHT 11호기', 'OHT', 'A사', 'FAB-B Line3', '2023-06-01', 'RUNNING'),
('OHT-12', 'OHT 12호기', 'OHT', 'A사', 'FAB-B Line3', '2023-06-01', 'RUNNING'),
('OHT-13', 'OHT 13호기', 'OHT', 'B사', 'FAB-C Line1', '2023-07-18', 'RUNNING'),
('OHT-14', 'OHT 14호기', 'OHT', 'B사', 'FAB-C Line1', '2023-07-18', 'STOPPED'),
('OHT-15', 'OHT 15호기', 'OHT', 'B사', 'FAB-C Line2', '2023-08-22', 'RUNNING'),
('OHT-16', 'OHT 16호기', 'OHT', 'C사', 'FAB-C Line2', '2023-08-22', 'RUNNING'),
('OHT-17', 'OHT 17호기', 'OHT', 'C사', 'FAB-C Line3', '2023-09-10', 'RUNNING'),
('OHT-18', 'OHT 18호기', 'OHT', 'C사', 'FAB-C Line3', '2023-09-10', 'RUNNING'),
('OHT-19', 'OHT 19호기', 'OHT', 'A사', 'FAB-A Line1', '2023-10-05', 'MAINTENANCE'),
('OHT-20', 'OHT 20호기', 'OHT', 'B사', 'FAB-A Line2', '2023-10-05', 'RUNNING'),
-- AGV 20대 (제조사: D사, E사)
('AGV-01', 'AGV 1호기', 'AGV', 'D사', 'FAB-A Line1', '2023-01-20', 'RUNNING'),
('AGV-02', 'AGV 2호기', 'AGV', 'D사', 'FAB-A Line1', '2023-01-20', 'RUNNING'),
('AGV-03', 'AGV 3호기', 'AGV', 'D사', 'FAB-A Line2', '2023-02-15', 'RUNNING'),
('AGV-04', 'AGV 4호기', 'AGV', 'D사', 'FAB-A Line3', '2023-02-15', 'STOPPED'),
('AGV-05', 'AGV 5호기', 'AGV', 'E사', 'FAB-A Line3', '2023-03-25', 'RUNNING'),
('AGV-06', 'AGV 6호기', 'AGV', 'E사', 'FAB-B Line1', '2023-03-25', 'RUNNING'),
('AGV-07', 'AGV 7호기', 'AGV', 'E사', 'FAB-B Line1', '2023-04-10', 'RUNNING'),
('AGV-08', 'AGV 8호기', 'AGV', 'E사', 'FAB-B Line2', '2023-04-10', 'MAINTENANCE'),
('AGV-09', 'AGV 9호기', 'AGV', 'D사', 'FAB-B Line2', '2023-05-18', 'RUNNING'),
('AGV-10', 'AGV 10호기', 'AGV', 'D사', 'FAB-B Line3', '2023-05-18', 'RUNNING'),
('AGV-11', 'AGV 11호기', 'AGV', 'D사', 'FAB-B Line3', '2023-06-08', 'RUNNING'),
('AGV-12', 'AGV 12호기', 'AGV', 'E사', 'FAB-C Line1', '2023-06-08', 'RUNNING'),
('AGV-13', 'AGV 13호기', 'AGV', 'E사', 'FAB-C Line1', '2023-07-22', 'STOPPED'),
('AGV-14', 'AGV 14호기', 'AGV', 'E사', 'FAB-C Line2', '2023-07-22', 'RUNNING'),
('AGV-15', 'AGV 15호기', 'AGV', 'E사', 'FAB-C Line2', '2023-08-28', 'RUNNING'),
('AGV-16', 'AGV 16호기', 'AGV', 'D사', 'FAB-C Line3', '2023-08-28', 'RUNNING'),
('AGV-17', 'AGV 17호기', 'AGV', 'D사', 'FAB-C Line3', '2023-09-15', 'RUNNING'),
('AGV-18', 'AGV 18호기', 'AGV', 'D사', 'FAB-A Line2', '2023-09-15', 'RUNNING'),
('AGV-19', 'AGV 19호기', 'AGV', 'E사', 'FAB-A Line3', '2023-10-10', 'RUNNING'),
('AGV-20', 'AGV 20호기', 'AGV', 'E사', 'FAB-B Line1', '2023-10-10', 'MAINTENANCE');

-- -----------------------------------------------
-- 2. 임계값 규칙 16행 (센서 8종 x 장비유형 2종)
-- -----------------------------------------------
INSERT IGNORE INTO threshold_rule (eq_type, sensor_name, caution_value, warning_value, danger_value) VALUES
-- OHT 임계값
('OHT', 'pm10',        80,    150,    300),
('OHT', 'pm25',        35,     75,    150),
('OHT', 'ntc_temp',    40,     55,     70),
('OHT', 'ct1',          3,      5,      8),
('OHT', 'ct2',          3,      5,      8),
('OHT', 'ct3',          3,      5,      8),
('OHT', 'ct4',          3,      5,      8),
('OHT', 'ir_temp_max', 45,     60,     80),
-- AGV 임계값
('AGV', 'pm10',        80,    150,    300),
('AGV', 'pm25',        35,     75,    150),
('AGV', 'ntc_temp',    45,     60,     75),
('AGV', 'ct1',          4,      7,     10),
('AGV', 'ct2',          4,      7,     10),
('AGV', 'ct3',          4,      7,     10),
('AGV', 'ct4',          4,      7,     10),
('AGV', 'ir_temp_max', 50,     65,     85);

-- -----------------------------------------------
-- 3. 사용자 3명 (비밀번호: password123 → BCrypt 해시)
-- -----------------------------------------------
INSERT IGNORE INTO `user` (username, password, name, role) VALUES
('admin',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '관리자',    'ADMIN'),
('engineer1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '엔지니어1', 'ENGINEER'),
('engineer2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '엔지니어2', 'ENGINEER');
