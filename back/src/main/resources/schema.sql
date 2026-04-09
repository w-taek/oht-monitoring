-- =============================================
-- OHT/AGV 예지보전 모니터링 시스템 DDL
-- =============================================

CREATE TABLE IF NOT EXISTS equipment (
    eq_id VARCHAR(20) PRIMARY KEY,
    eq_name VARCHAR(100) NOT NULL,
    eq_type ENUM('OHT','AGV') NOT NULL,
    manufacturer VARCHAR(50),
    location VARCHAR(100),
    install_date DATE,
    status ENUM('RUNNING','STOPPED','MAINTENANCE') DEFAULT 'RUNNING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sensor_reading (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eq_id VARCHAR(20) NOT NULL,
    collected_at TIMESTAMP NOT NULL,
    pm10 DOUBLE,
    pm25 DOUBLE,
    pm10_val DOUBLE,
    ntc_temp DOUBLE,
    ct1 DOUBLE,
    ct2 DOUBLE,
    ct3 DOUBLE,
    ct4 DOUBLE,
    ir_temp_max DOUBLE,
    ir_x INT,
    ir_y INT,
    ex_temp DOUBLE,
    ex_humidity DOUBLE,
    ex_lux DOUBLE,
    state TINYINT DEFAULT 0,
    cumulative_operating_day INT,
    equipment_history INT,
    FOREIGN KEY (eq_id) REFERENCES equipment(eq_id),
    INDEX idx_eq_collected (eq_id, collected_at),
    INDEX idx_state (state)
);

CREATE TABLE IF NOT EXISTS threshold_rule (
    id INT AUTO_INCREMENT PRIMARY KEY,
    eq_type ENUM('OHT','AGV') NOT NULL,
    sensor_name VARCHAR(30) NOT NULL,
    caution_value DOUBLE NOT NULL,
    warning_value DOUBLE NOT NULL,
    danger_value DOUBLE NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS alert_event (
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

CREATE TABLE IF NOT EXISTS maint_order (
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

CREATE TABLE IF NOT EXISTS `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(200) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN','ENGINEER','OPERATOR','VIEWER') DEFAULT 'ENGINEER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
