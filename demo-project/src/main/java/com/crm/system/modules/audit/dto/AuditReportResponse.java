package com.crm.system.modules.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审计日志上报响应DTO
 *
 * <p>封装外部审计系统的响应结果</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditReportResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 外部系统返回的审计ID
     */
    private String auditId;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 接收时间
     */
    private String receivedAt;

    /**
     * 创建成功响应
     *
     * @param auditId 审计ID
     * @return 响应对象
     */
    public static AuditReportResponse success(String auditId) {
        return AuditReportResponse.builder()
                .success(true)
                .message("上报成功")
                .auditId(auditId)
                .build();
    }

    /**
     * 创建失败响应
     *
     * @param message 错误消息
     * @param errorCode 错误码
     * @return 响应对象
     */
    public static AuditReportResponse failure(String message, String errorCode) {
        return AuditReportResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
