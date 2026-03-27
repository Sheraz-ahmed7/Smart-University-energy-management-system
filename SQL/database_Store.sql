-- ============================================================
--  SEUS — Smart Energy Optimization System for Universities
--  Database Schema  (MySQL 8+)
-- ============================================================

CREATE DATABASE IF NOT EXISTS university_energy_mgt
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE university_energy_mgt;

-- ── Drop in reverse-dependency order ─────────────────────────
DROP TABLE IF EXISTS energy_usage;
DROP TABLE IF EXISTS devices;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS departments;

-- ── 1. Departments ────────────────────────────────────────────
CREATE TABLE departments (
    dept_id        INT          AUTO_INCREMENT PRIMARY KEY,
    dept_name      VARCHAR(100) NOT NULL UNIQUE,
    floor_number   INT          NOT NULL DEFAULT 0,
    contact_number VARCHAR(20)  DEFAULT NULL,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ── 2. Users ─────────────────────────────────────────────────
CREATE TABLE users (
    user_id       INT           AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(60)   NOT NULL UNIQUE,
    password      VARCHAR(64)   NOT NULL,          -- SHA-256 hex
    role          ENUM('admin','staff') NOT NULL DEFAULT 'staff',
    department_id INT           DEFAULT NULL,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_dept
        FOREIGN KEY (department_id) REFERENCES departments(dept_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- ── 3. Devices ────────────────────────────────────────────────
CREATE TABLE devices (
    device_id    INT           AUTO_INCREMENT PRIMARY KEY,
    dept_id      INT           NOT NULL,
    device_name  VARCHAR(100)  NOT NULL,
    wattage      INT           NOT NULL CHECK (wattage > 0),
    quantity     INT           NOT NULL DEFAULT 1 CHECK (quantity > 0),
    hours_per_day DOUBLE       NOT NULL DEFAULT 8.0 CHECK (hours_per_day >= 0 AND hours_per_day <= 24),
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_device_dept
        FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ── 4. Energy Usage History ────────────────────────────────────
CREATE TABLE energy_usage (
    usage_id         INT        AUTO_INCREMENT PRIMARY KEY,
    dept_id          INT        NOT NULL,
    timestamp        TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    kwh              DOUBLE     NOT NULL DEFAULT 0,
    cost             DOUBLE     NOT NULL DEFAULT 0,
    carbon_footprint DOUBLE     NOT NULL DEFAULT 0,

    CONSTRAINT fk_usage_dept
        FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_usage_dept_time (dept_id, timestamp)
);

-- ============================================================
--  SEED DATA
-- ============================================================

-- Departments
INSERT INTO departments (dept_name, floor_number, contact_number) VALUES
    ('Computer Science',        3, '0300-1110001'),
    ('Electrical Engineering',  2, '0300-1110002'),
    ('Civil Engineering',       1, '0300-1110003'),
    ('Business Administration', 4, '0300-1110004'),
    ('Physics',                 2, '0300-1110005'),
    ('Mathematics',             1, '0300-1110006'),
    ('Library',                 0, '0300-1110007');

-- Users
-- Passwords are SHA-256 of the plain-text shown in comments
--   admin123  → 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
--   staff123  → 2935a157ff97c27a01b6e4ca1a91234edd7f7a5f5d4e5de2f0e66e7d59c6d8a2
--   (generate your own: echo -n "yourpass" | sha256sum)

INSERT INTO users (username, password, role, department_id) VALUES
    ('admin',
     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
     'admin', NULL),
    ('cs_staff',
     '4a44dc15364204a80fe80e9039455cc1608281820fe2b24f1e5233ade6af1dd5',
     'staff',
     (SELECT dept_id FROM departments WHERE dept_name='Computer Science')),
    ('ee_staff',
     '4a44dc15364204a80fe80e9039455cc1608281820fe2b24f1e5233ade6af1dd5',
     'staff',
     (SELECT dept_id FROM departments WHERE dept_name='Electrical Engineering'));

-- NOTE: cs_staff and ee_staff password is SHA-256 of "staff123"
-- To change, run:  SELECT SHA2('yourNewPassword', 256);

-- Devices — Computer Science
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Desktop Computer', 300, 40, 8 FROM departments WHERE dept_name='Computer Science';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'LED Monitor',       30, 40, 8 FROM departments WHERE dept_name='Computer Science';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Air Conditioner', 1500,  4, 9 FROM departments WHERE dept_name='Computer Science';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'LED Tube Light',   18, 20, 10 FROM departments WHERE dept_name='Computer Science';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Projector',       250,  2,  6 FROM departments WHERE dept_name='Computer Science';

-- Devices — Electrical Engineering
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Oscilloscope',    100, 15, 5 FROM departments WHERE dept_name='Electrical Engineering';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Lab Power Supply', 200, 20, 6 FROM departments WHERE dept_name='Electrical Engineering';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Air Conditioner', 1800,  3, 8 FROM departments WHERE dept_name='Electrical Engineering';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'LED Tube Light',   18, 25, 10 FROM departments WHERE dept_name='Electrical Engineering';

-- Devices — Business Administration
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Desktop Computer', 280, 30, 7 FROM departments WHERE dept_name='Business Administration';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Printer',          400,  4, 4 FROM departments WHERE dept_name='Business Administration';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Air Conditioner', 1500,  5, 8 FROM departments WHERE dept_name='Business Administration';

-- Devices — Library
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'LED Tube Light',   18, 50, 12 FROM departments WHERE dept_name='Library';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Desktop Computer', 300,  8,  8 FROM departments WHERE dept_name='Library';
INSERT INTO devices (dept_id, device_name, wattage, quantity, hours_per_day)
SELECT dept_id, 'Air Conditioner', 2000,  2, 10 FROM departments WHERE dept_name='Library';

-- Energy usage history (last 60 days, deterministic values)
-- Inserts one record per day per department using a stored procedure

DELIMITER $$
CREATE PROCEDURE seed_energy_history()
BEGIN
    DECLARE i INT DEFAULT 60;
    DECLARE d_id INT;
    DECLARE base_kwh DOUBLE;

    WHILE i >= 1 DO
        -- CS
        SET d_id = (SELECT dept_id FROM departments WHERE dept_name='Computer Science');
        SET base_kwh = 120.0 + (MOD(i * 7 + d_id * 13, 30) - 15);
        INSERT INTO energy_usage (dept_id, timestamp, kwh, cost, carbon_footprint)
        VALUES (d_id, DATE_SUB(NOW(), INTERVAL i DAY), base_kwh, base_kwh*25, base_kwh*0.5);

        -- EE
        SET d_id = (SELECT dept_id FROM departments WHERE dept_name='Electrical Engineering');
        SET base_kwh = 95.0 + (MOD(i * 11 + d_id * 7, 20) - 10);
        INSERT INTO energy_usage (dept_id, timestamp, kwh, cost, carbon_footprint)
        VALUES (d_id, DATE_SUB(NOW(), INTERVAL i DAY), base_kwh, base_kwh*25, base_kwh*0.5);

        -- BA
        SET d_id = (SELECT dept_id FROM departments WHERE dept_name='Business Administration');
        SET base_kwh = 85.0 + (MOD(i * 5 + d_id * 17, 25) - 12);
        INSERT INTO energy_usage (dept_id, timestamp, kwh, cost, carbon_footprint)
        VALUES (d_id, DATE_SUB(NOW(), INTERVAL i DAY), base_kwh, base_kwh*25, base_kwh*0.5);

        SET i = i - 1;
    END WHILE;
END$$
DELIMITER ;

CALL seed_energy_history();
DROP PROCEDURE IF EXISTS seed_energy_history;

-- ── Verify ──────────────────────────────────────────────────
SELECT 'Departments' AS table_name, COUNT(*) AS rows FROM departments
UNION ALL SELECT 'Users',          COUNT(*) FROM users
UNION ALL SELECT 'Devices',        COUNT(*) FROM devices
UNION ALL SELECT 'Energy Usage',   COUNT(*) FROM energy_usage;