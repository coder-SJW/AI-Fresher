-- =====================================================
-- CRM 客户管理系统数据库初始化脚本
-- 版本: 1.0
-- 日期: 2026-01-12
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS crm_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE crm_db;

-- =====================================================
-- 1. 角色表
-- =====================================================
CREATE TABLE crm_role (
    id BIGINT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0:正常 1:删除）',

    UNIQUE KEY uk_role_code (role_code),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 初始化角色数据
INSERT INTO crm_role (id, role_name, role_code, description) VALUES
(1, '销售代表', 'SALES_REP', '负责客户跟进和销售机会管理'),
(2, '销售经理', 'SALES_MANAGER', '负责销售团队管理和数据查看'),
(3, '系统管理员', 'ADMIN', '负责系统配置和用户管理');

-- =====================================================
-- 2. 用户表
-- =====================================================
CREATE TABLE crm_user (
    id BIGINT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1:启用 0:禁用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE KEY uk_username (username),
    KEY idx_role_id (role_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES crm_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 初始化管理员用户（密码：123456，已BCrypt加密）
INSERT INTO crm_user (id, username, password, real_name, role_id, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 3, 1);

-- =====================================================
-- 3. 数据字典表
-- =====================================================
CREATE TABLE crm_dict (
    id BIGINT PRIMARY KEY COMMENT '字典ID',
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1:启用 0:禁用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',

    KEY idx_dict_type (dict_type),
    UNIQUE KEY uk_type_value (dict_type, dict_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 初始化客户状态字典
INSERT INTO crm_dict (dict_type, dict_label, dict_value, sort_order) VALUES
('customer_status', '潜在客户', 'POTENTIAL', 1),
('customer_status', '意向客户', 'INTENTION', 2),
('customer_status', '成交客户', 'DEAL', 3),
('customer_status', '流失客户', 'LOST', 4);

-- 初始化客户来源字典
INSERT INTO crm_dict (dict_type, dict_label, dict_value, sort_order) VALUES
('customer_source', '线上推广', 'ONLINE', 1),
('customer_source', '线下活动', 'OFFLINE', 2),
('customer_source', '客户转介绍', 'REFERRAL', 3),
('customer_source', '主动咨询', 'INQUIRY', 4),
('customer_source', '其他', 'OTHER', 5);

-- 初始化跟进方式字典
INSERT INTO crm_dict (dict_type, dict_label, dict_value, sort_order) VALUES
('follow_up_method', '电话', 'PHONE', 1),
('follow_up_method', '微信', 'WECHAT', 2),
('follow_up_method', '邮件', 'EMAIL', 3),
('follow_up_method', '上门拜访', 'VISIT', 4),
('follow_up_method', '其他', 'OTHER', 5);

-- 初始化销售阶段字典
INSERT INTO crm_dict (dict_type, dict_label, dict_value, sort_order) VALUES
('sales_stage', '线索确认', 'LEAD', 1),
('sales_stage', '需求分析', 'ANALYSIS', 2),
('sales_stage', '方案报价', 'PROPOSAL', 3),
('sales_stage', '谈判协商', 'NEGOTIATION', 4),
('sales_stage', '合同签署', 'CONTRACT', 5),
('sales_stage', '已成交', 'WON', 6);

-- =====================================================
-- 4. 客户表
-- =====================================================
CREATE TABLE crm_customer (
    id BIGINT PRIMARY KEY COMMENT '客户ID',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    company_address VARCHAR(255) COMMENT '公司地址',
    customer_source VARCHAR(50) COMMENT '客户来源（字典值）',
    customer_status VARCHAR(20) NOT NULL DEFAULT 'POTENTIAL' COMMENT '客户状态（字典值）',
    remark TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    KEY idx_customer_name (customer_name),
    KEY idx_contact_phone (contact_phone),
    KEY idx_customer_status (customer_status),
    KEY idx_customer_source (customer_source),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- =====================================================
-- 5. 跟进记录表
-- =====================================================
CREATE TABLE crm_follow_up (
    id BIGINT PRIMARY KEY COMMENT '记录ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    follow_up_content TEXT NOT NULL COMMENT '跟进内容',
    follow_up_method VARCHAR(20) NOT NULL COMMENT '跟进方式（字典值）',
    next_follow_up_time DATETIME COMMENT '下次跟进时间',
    follow_up_person BIGINT NOT NULL COMMENT '跟进人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',

    KEY idx_customer_id (customer_id),
    KEY idx_follow_up_person (follow_up_person),
    KEY idx_next_follow_up_time (next_follow_up_time),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_followup_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id),
    CONSTRAINT fk_followup_person FOREIGN KEY (follow_up_person) REFERENCES crm_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跟进记录表';

-- =====================================================
-- 6. 销售机会表
-- =====================================================
CREATE TABLE crm_opportunity (
    id BIGINT PRIMARY KEY COMMENT '机会ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    opportunity_name VARCHAR(200) NOT NULL COMMENT '机会名称',
    estimated_amount DECIMAL(12,2) NOT NULL COMMENT '预计金额',
    estimated_close_date DATE NOT NULL COMMENT '预计成交日期',
    sales_stage VARCHAR(50) NOT NULL COMMENT '销售阶段（字典值）',
    probability INT COMMENT '成交概率（0-100）',
    remark TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    KEY idx_customer_id (customer_id),
    KEY idx_sales_stage (sales_stage),
    KEY idx_estimated_close_date (estimated_close_date),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_opportunity_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售机会表';

-- =====================================================
-- 索引说明
-- =====================================================
-- 1. 所有表都有主键索引
-- 2. 所有外键字段都创建了索引
-- 3. 常用查询字段（如状态、时间）都创建了索引
-- 4. 用户名、角色编码等唯一字段创建了唯一索引

-- =====================================================
-- 初始化完成
-- =====================================================
-- 默认管理员账号: admin
-- 默认密码: 123456
-- 请在生产环境中立即修改密码！
