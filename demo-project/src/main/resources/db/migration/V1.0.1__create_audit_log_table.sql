-- =====================================================
-- 审计日志表创建脚本
-- 版本: V1.0.1
-- 描述: 创建审计日志表，用于记录业务数据的操作审计
-- 作者: AI Assistant
-- 日期: 2025-01-12
-- =====================================================

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS `crm_audit_log` (
    `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
    `module_name` VARCHAR(50) NOT NULL COMMENT '模块名称（customer、opportunity等）',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型: CREATE/UPDATE/DELETE',
    `business_id` BIGINT NOT NULL COMMENT '业务数据ID（客户ID、销售机会ID等）',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) NOT NULL COMMENT '操作人姓名',
    `operate_time` DATETIME NOT NULL COMMENT '操作时间',
    `before_data` JSON COMMENT '变更前数据（JSON格式）',
    `after_data` JSON COMMENT '变更后数据（JSON格式）',
    `changed_fields` JSON COMMENT '变更字段列表（JSON数组）',
    `report_status` TINYINT NOT NULL DEFAULT 0 COMMENT '上报状态: 0-待上报，1-已上报，2-上报失败',
    `fail_reason` VARCHAR(500) COMMENT '上报失败原因',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '上报重试次数',
    `last_report_time` DATETIME COMMENT '最后上报时间',
    `client_ip` VARCHAR(50) COMMENT '客户端IP地址',
    `trace_id` VARCHAR(64) COMMENT '请求追踪ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_module_business` (`module_name`, `business_id`),
    INDEX `idx_operator` (`operator_id`),
    INDEX `idx_operate_time` (`operate_time`),
    INDEX `idx_report_status` (`report_status`),
    INDEX `idx_report_retry` (`report_status`, `retry_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- =====================================================
-- 索引说明
-- =====================================================
-- PRIMARY KEY: 主键索引，基于ID
-- idx_module_business: 复合索引，用于快速查询某业务的所有审计日志
-- idx_operator: 单列索引，用于查询某人的所有操作
-- idx_operate_time: 单列索引，用于按时间范围查询
-- idx_report_status: 单列索引，用于扫描待上报或失败的记录
-- idx_report_retry: 复合索引，用于定时任务扫描失败的记录
-- =====================================================
