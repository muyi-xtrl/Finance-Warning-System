-- ============================================================
-- 金融预警大数据管理平台 - MySQL 初始化脚本
-- 数据库: finance_warning
-- ============================================================

CREATE DATABASE IF NOT EXISTS finance_warning DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finance_warning;

-- ----------------------------
-- 系统用户
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(64)  NOT NULL UNIQUE COMMENT '登录名',
  password      VARCHAR(128) NOT NULL COMMENT 'BCrypt密码',
  real_name     VARCHAR(64)  DEFAULT NULL COMMENT '姓名',
  role          VARCHAR(32)  NOT NULL DEFAULT 'ANALYST' COMMENT 'ADMIN/ANALYST/VIEWER',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统用户';

-- 默认密码均为 123456 (BCrypt)
INSERT INTO sys_user (username, password, real_name, role) VALUES
('admin',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN'),
('analyst', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '风险分析师', 'ANALYST'),
('viewer',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '只读用户',   'VIEWER');

-- ----------------------------
-- 企业/客户主体
-- ----------------------------
DROP TABLE IF EXISTS enterprise;
CREATE TABLE enterprise (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  ent_code        VARCHAR(32)  NOT NULL UNIQUE COMMENT '企业编码',
  ent_name        VARCHAR(128) NOT NULL COMMENT '企业名称',
  industry        VARCHAR(64)  DEFAULT NULL COMMENT '所属行业',
  region          VARCHAR(64)  DEFAULT NULL COMMENT '所在地区',
  credit_level    VARCHAR(8)   DEFAULT 'B' COMMENT '信用等级 A/B/C/D',
  registered_cap  DECIMAL(18,2) DEFAULT 0 COMMENT '注册资本(万)',
  status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0注销',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='企业主体';

INSERT INTO enterprise (ent_code, ent_name, industry, region, credit_level, registered_cap) VALUES
('ENT001', '华信科技股份有限公司',   '信息技术', '北京', 'A', 5000.00),
('ENT002', '锦程制造集团有限公司',   '制造业',   '江苏', 'B', 8000.00),
('ENT003', '盛达贸易有限公司',       '批发零售', '上海', 'B', 2000.00),
('ENT004', '绿能新能源有限公司',     '能源',     '广东', 'A', 12000.00),
('ENT005', '远航物流股份有限公司',   '交通运输', '浙江', 'C', 3500.00),
('ENT006', '金鼎房地产开发有限公司', '房地产',   '四川', 'C', 15000.00),
('ENT007', '安康医药集团有限公司',   '医药生物', '湖北', 'A', 6000.00),
('ENT008', '星河互联网金融有限公司', '金融',     '深圳', 'D', 1000.00),
('ENT009', '博雅教育科技有限公司',   '教育',     '陕西', 'B', 1500.00),
('ENT010', '海川食品股份有限公司',   '食品饮料', '山东', 'B', 4200.00);

-- ----------------------------
-- 金融交易明细（业务库镜像；大数据原始数据在 HDFS）
-- ----------------------------
DROP TABLE IF EXISTS finance_transaction;
CREATE TABLE finance_transaction (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  txn_id          VARCHAR(64)  NOT NULL UNIQUE COMMENT '交易流水号',
  ent_code        VARCHAR(32)  NOT NULL COMMENT '企业编码',
  txn_date        DATE         NOT NULL COMMENT '交易日期',
  txn_type        VARCHAR(32)  NOT NULL COMMENT 'LOAN/REPAY/OVERDUE/GUARANTEE',
  amount          DECIMAL(18,2) NOT NULL COMMENT '金额(元)',
  balance         DECIMAL(18,2) DEFAULT 0 COMMENT '剩余本金',
  overdue_days    INT          DEFAULT 0 COMMENT '逾期天数',
  interest_rate   DECIMAL(8,4) DEFAULT 0 COMMENT '利率',
  risk_flag       TINYINT      DEFAULT 0 COMMENT '0正常 1可疑',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_ent_date (ent_code, txn_date),
  KEY idx_type (txn_type)
) ENGINE=InnoDB COMMENT='金融交易明细';

INSERT INTO finance_transaction (txn_id, ent_code, txn_date, txn_type, amount, balance, overdue_days, interest_rate, risk_flag) VALUES
('TXN20240101001', 'ENT001', '2024-01-05', 'LOAN',      5000000.00, 4200000.00, 0,  0.0450, 0),
('TXN20240115001', 'ENT001', '2024-01-15', 'REPAY',      800000.00, 4200000.00, 0,  0.0450, 0),
('TXN20240201001', 'ENT002', '2024-02-01', 'LOAN',      8000000.00, 8000000.00, 0,  0.0520, 0),
('TXN20240220001', 'ENT002', '2024-02-20', 'OVERDUE',         0.00, 8000000.00, 15, 0.0520, 1),
('TXN20240301001', 'ENT003', '2024-03-01', 'LOAN',      2000000.00, 1800000.00, 0,  0.0600, 0),
('TXN20240310001', 'ENT005', '2024-03-10', 'LOAN',      3500000.00, 3500000.00, 0,  0.0580, 0),
('TXN20240325001', 'ENT005', '2024-03-25', 'OVERDUE',         0.00, 3500000.00, 32, 0.0580, 1),
('TXN20240401001', 'ENT006', '2024-04-01', 'LOAN',     20000000.00,20000000.00, 0,  0.0650, 0),
('TXN20240415001', 'ENT006', '2024-04-15', 'OVERDUE',         0.00,20000000.00, 45, 0.0650, 1),
('TXN20240501001', 'ENT008', '2024-05-01', 'LOAN',      1500000.00, 1500000.00, 0,  0.0800, 0),
('TXN20240520001', 'ENT008', '2024-05-20', 'OVERDUE',         0.00, 1500000.00, 60, 0.0800, 1),
('TXN20240601001', 'ENT004', '2024-06-01', 'LOAN',     10000000.00, 9000000.00, 0,  0.0420, 0),
('TXN20240615001', 'ENT004', '2024-06-15', 'REPAY',     1000000.00, 9000000.00, 0,  0.0420, 0),
('TXN20240701001', 'ENT007', '2024-07-01', 'LOAN',      6000000.00, 5500000.00, 0,  0.0480, 0),
('TXN20240720001', 'ENT007', '2024-07-20', 'GUARANTEE', 3000000.00, 0.00,       0,  0.0000, 0),
('TXN20240801001', 'ENT009', '2024-08-01', 'LOAN',      1200000.00, 1200000.00, 0,  0.0550, 0),
('TXN20240815001', 'ENT010', '2024-08-15', 'LOAN',      4000000.00, 3800000.00, 0,  0.0500, 0),
('TXN20240901001', 'ENT003', '2024-09-01', 'OVERDUE',         0.00, 1800000.00, 20, 0.0600, 1),
('TXN20240910001', 'ENT005', '2024-09-10', 'OVERDUE',         0.00, 3500000.00, 90, 0.0580, 1),
('TXN20240920001', 'ENT008', '2024-09-20', 'OVERDUE',         0.00, 1500000.00,120, 0.0800, 1);

-- ----------------------------
-- 预警规则
-- ----------------------------
DROP TABLE IF EXISTS warning_rule;
CREATE TABLE warning_rule (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_code       VARCHAR(32)  NOT NULL UNIQUE COMMENT '规则编码',
  rule_name       VARCHAR(128) NOT NULL COMMENT '规则名称',
  rule_type       VARCHAR(32)  NOT NULL COMMENT 'OVERDUE/AMOUNT/CONCENTRATION/SCORE',
  threshold_value DECIMAL(18,4) NOT NULL COMMENT '阈值',
  level           VARCHAR(16)  NOT NULL COMMENT 'LOW/MEDIUM/HIGH/CRITICAL',
  description     VARCHAR(512) DEFAULT NULL,
  enabled         TINYINT      NOT NULL DEFAULT 1,
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='预警规则';

INSERT INTO warning_rule (rule_code, rule_name, rule_type, threshold_value, level, description, enabled) VALUES
('R001', '逾期天数预警(中)',   'OVERDUE',       30.0000, 'MEDIUM',   '逾期超过30天触发中级预警', 1),
('R002', '逾期天数预警(高)',   'OVERDUE',       60.0000, 'HIGH',     '逾期超过60天触发高级预警', 1),
('R003', '逾期天数预警(危)',   'OVERDUE',      90.0000, 'CRITICAL', '逾期超过90天触发危急预警', 1),
('R004', '单笔贷款金额预警',   'AMOUNT',  10000000.0000, 'HIGH',     '单笔贷款超过1000万', 1),
('R005', '行业集中度预警',     'CONCENTRATION', 0.3000, 'MEDIUM',   '同一行业贷款占比超过30%', 1),
('R006', '综合风险评分预警',   'SCORE',        70.0000, 'HIGH',     'MapReduce计算风险分>=70', 1);

-- ----------------------------
-- 预警记录（含 MR 计算结果回写）
-- ----------------------------
DROP TABLE IF EXISTS warning_record;
CREATE TABLE warning_record (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  ent_code        VARCHAR(32)  NOT NULL,
  ent_name        VARCHAR(128) DEFAULT NULL,
  rule_code       VARCHAR(32)  DEFAULT NULL,
  warning_level   VARCHAR(16)  NOT NULL COMMENT 'LOW/MEDIUM/HIGH/CRITICAL',
  risk_score      DECIMAL(10,2) DEFAULT 0 COMMENT '风险评分0-100',
  title           VARCHAR(256) NOT NULL,
  content         VARCHAR(1024) DEFAULT NULL,
  source          VARCHAR(32)  NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM/MR/MANUAL',
  status          VARCHAR(16)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/PROCESSING/CLOSED',
  warning_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  handle_remark   VARCHAR(512) DEFAULT NULL,
  handle_time     DATETIME     DEFAULT NULL,
  KEY idx_ent (ent_code),
  KEY idx_level (warning_level),
  KEY idx_status (status),
  KEY idx_time (warning_time)
) ENGINE=InnoDB COMMENT='预警记录';

INSERT INTO warning_record (ent_code, ent_name, rule_code, warning_level, risk_score, title, content, source, status, warning_time) VALUES
('ENT005', '远航物流股份有限公司',   'R003', 'CRITICAL', 88.50, '逾期超90天危急预警', '当前逾期90天，剩余本金350万，建议立即催收并冻结新增授信', 'MR', 'OPEN', '2024-09-11 09:00:00'),
('ENT008', '星河互联网金融有限公司', 'R003', 'CRITICAL', 95.20, '逾期超120天危急预警', '逾期120天且信用等级D，综合风险极高', 'MR', 'PROCESSING', '2024-09-21 10:30:00'),
('ENT006', '金鼎房地产开发有限公司', 'R004', 'HIGH',     76.00, '大额贷款风险预警', '单笔贷款2000万且已逾期45天', 'MR', 'OPEN', '2024-04-16 11:00:00'),
('ENT002', '锦程制造集团有限公司',   'R001', 'MEDIUM',   55.00, '逾期30天中级预警', '逾期15天接近阈值，需关注还款计划', 'SYSTEM', 'CLOSED', '2024-02-21 08:20:00'),
('ENT003', '盛达贸易有限公司',       'R001', 'MEDIUM',   52.30, '逾期中级预警', '逾期20天，余额180万', 'MR', 'OPEN', '2024-09-02 14:00:00');

-- ----------------------------
-- 企业风险评分汇总（MR 输出汇总表）
-- ----------------------------
DROP TABLE IF EXISTS enterprise_risk_score;
CREATE TABLE enterprise_risk_score (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  ent_code        VARCHAR(32)  NOT NULL,
  ent_name        VARCHAR(128) DEFAULT NULL,
  total_loan      DECIMAL(18,2) DEFAULT 0 COMMENT '贷款总额',
  total_balance   DECIMAL(18,2) DEFAULT 0 COMMENT '剩余本金',
  max_overdue     INT          DEFAULT 0 COMMENT '最大逾期天数',
  overdue_count   INT          DEFAULT 0 COMMENT '逾期次数',
  risk_score      DECIMAL(10,2) DEFAULT 0 COMMENT '综合风险分',
  risk_level      VARCHAR(16)  DEFAULT 'LOW',
  calc_batch      VARCHAR(64)  DEFAULT NULL COMMENT '计算批次号',
  calc_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ent_batch (ent_code, calc_batch)
) ENGINE=InnoDB COMMENT='企业风险评分(MR结果)';

INSERT INTO enterprise_risk_score (ent_code, ent_name, total_loan, total_balance, max_overdue, overdue_count, risk_score, risk_level, calc_batch) VALUES
('ENT001', '华信科技股份有限公司',    5000000,  4200000,  0, 0, 22.00, 'LOW',      'BATCH_DEMO'),
('ENT002', '锦程制造集团有限公司',    8000000,  8000000, 15, 1, 48.00, 'MEDIUM',   'BATCH_DEMO'),
('ENT003', '盛达贸易有限公司',        2000000,  1800000, 20, 1, 52.30, 'MEDIUM',   'BATCH_DEMO'),
('ENT004', '绿能新能源有限公司',     10000000,  9000000,  0, 0, 18.50, 'LOW',      'BATCH_DEMO'),
('ENT005', '远航物流股份有限公司',    3500000,  3500000, 90, 2, 88.50, 'CRITICAL', 'BATCH_DEMO'),
('ENT006', '金鼎房地产开发有限公司', 20000000, 20000000, 45, 1, 76.00, 'HIGH',     'BATCH_DEMO'),
('ENT007', '安康医药集团有限公司',    6000000,  5500000,  0, 0, 20.00, 'LOW',      'BATCH_DEMO'),
('ENT008', '星河互联网金融有限公司',  1500000,  1500000,120, 2, 95.20, 'CRITICAL', 'BATCH_DEMO'),
('ENT009', '博雅教育科技有限公司',    1200000,  1200000,  0, 0, 25.00, 'LOW',      'BATCH_DEMO'),
('ENT010', '海川食品股份有限公司',    4000000,  3800000,  0, 0, 28.00, 'LOW',      'BATCH_DEMO');

-- ----------------------------
-- Hadoop 作业执行日志
-- ----------------------------
DROP TABLE IF EXISTS hadoop_job_log;
CREATE TABLE hadoop_job_log (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  job_name        VARCHAR(128) NOT NULL,
  job_id          VARCHAR(128) DEFAULT NULL COMMENT 'YARN ApplicationId',
  batch_no        VARCHAR(64)  NOT NULL,
  input_path      VARCHAR(512) NOT NULL,
  output_path     VARCHAR(512) NOT NULL,
  status          VARCHAR(32)  NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/RUNNING/SUCCEEDED/FAILED',
  message         VARCHAR(1024) DEFAULT NULL,
  start_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time        DATETIME     DEFAULT NULL
) ENGINE=InnoDB COMMENT='Hadoop作业日志';

-- ----------------------------
-- HDFS 文件登记
-- ----------------------------
DROP TABLE IF EXISTS hdfs_file_meta;
CREATE TABLE hdfs_file_meta (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_name       VARCHAR(256) NOT NULL,
  hdfs_path       VARCHAR(512) NOT NULL,
  file_size       BIGINT       DEFAULT 0,
  file_type       VARCHAR(32)  DEFAULT 'RAW' COMMENT 'RAW/RESULT/TMP',
  remark          VARCHAR(256) DEFAULT NULL,
  upload_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='HDFS文件元数据';

INSERT INTO hdfs_file_meta (file_name, hdfs_path, file_size, file_type, remark) VALUES
('transactions.csv', '/finance/raw/transactions.csv', 2048, 'RAW', '示例交易原始数据');

-- ----------------------------
-- 看板统计视图辅助：行业贷款占比可直接查 enterprise + score
-- ----------------------------
