package com.crm.system.modules.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志上报请求DTO
 *
 * <p>用于向外部审计系统上报审计日志</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditReportRequest {

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 操作类型（CREATE、UPDATE、DELETE）
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
     * 操作时间（ISO 8601格式）
     */
    private LocalDateTime operateTime;

    /**
     * 变更前数据
     */
    private Map<String, Object> beforeData;

    /**
     * 变更后数据
     */
    private Map<String, Object> afterData;

    /**
     * 变更字段列表
     */
    private List<String> changedFields;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 请求追踪ID
     */
    private String traceId;
}
