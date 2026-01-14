package com.crm.system.modules.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志响应DTO
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponse {

    /**
     * 审计日志ID
     */
    private Long id;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 业务数据ID
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
     * 变更前数据
     */
    private String beforeData;

    /**
     * 变更后数据
     */
    private String afterData;

    /**
     * 变更字段列表
     */
    private String changedFields;

    /**
     * 上报状态
     */
    private Integer reportStatus;

    /**
     * 上报状态描述
     */
    private String reportStatusDesc;

    /**
     * 上报失败原因
     */
    private String failReason;

    /**
     * 上报重试次数
     */
    private Integer retryCount;

    /**
     * 最后上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReportTime;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 请求追踪ID
     */
    private String traceId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
