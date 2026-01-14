package com.crm.system.modules.audit.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志实体类
 *
 * <p>记录所有业务数据的操作审计日志，包括创建、更新、删除操作</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("crm_audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模块名称
     * <p>如：customer、opportunity、follow等</p>
     */
    private String moduleName;

    /**
     * 操作类型
     * <p>CREATE-创建、UPDATE-更新、DELETE-删除</p>
     */
    private String operationType;

    /**
     * 业务数据ID
     * <p>如：客户ID、销售机会ID等</p>
     */
    private Long businessId;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;

    /**
     * 变更前数据（JSON格式）
     * <p>存储变更前的完整业务数据</p>
     */
    private String beforeData;

    /**
     * 变更后数据（JSON格式）
     * <p>存储变更后的完整业务数据</p>
     */
    private String afterData;

    /**
     * 变更字段列表（JSON格式）
     * <p>存储具体发生变化的字段列表</p>
     */
    private String changedFields;

    /**
     * 上报状态
     * <p>0-待上报，1-已上报，2-上报失败</p>
     */
    @Builder.Default
    private Integer reportStatus = 0;

    /**
     * 上报失败原因
     */
    private String failReason;

    /**
     * 上报重试次数
     */
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 最后上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReportTime;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 请求追踪ID
     * <p>用于链路追踪，关联多个相关的操作</p>
     */
    private String traceId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 上报状态枚举
     */
    public enum ReportStatus {
        PENDING(0, "待上报"),
        SUCCESS(1, "已上报"),
        FAILED(2, "上报失败");

        private final Integer code;
        private final String desc;

        ReportStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
