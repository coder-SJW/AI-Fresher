package com.crm.system.modules.audit.client;

import com.crm.system.modules.audit.domain.AuditLog;
import com.crm.system.modules.audit.dto.AuditReportRequest;
import com.crm.system.modules.audit.dto.AuditReportResponse;
import com.crm.system.modules.audit.util.DiffUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志上报REST客户端
 *
 * <p>负责调用外部审计系统的REST API接口</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuditReportClient {

    @Value("${audit.report.url:http://localhost:9000/api/audit/logs}")
    private String reportUrl;

    @Value("${audit.report.api-key:}")
    private String apiKey;

    @Value("${audit.report.enabled:true}")
    private boolean reportEnabled;

    @Value("${audit.report.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${audit.report.read-timeout:10000}")
    private int readTimeout;

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final DiffUtils diffUtils;
    private final ObjectMapper objectMapper;

    public AuditReportClient(RestTemplate restTemplate,
                              RetryTemplate retryTemplate,
                              DiffUtils diffUtils,
                              ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.diffUtils = diffUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * 上报审计日志到外部系统
     *
     * @param auditLog 审计日志
     * @return 是否上报成功
     */
    public boolean report(AuditLog auditLog) {
        // 检查是否启用上报
        if (!reportEnabled) {
            log.debug("审计上报功能已禁用");
            return true;
        }

        // 检查API Key
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("审计上报API Key未配置，跳过上报");
            return true;
        }

        return retryTemplate.execute(context -> {
            try {
                // 构建请求
                AuditReportRequest request = buildRequest(auditLog);

                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-API-Key", apiKey);
                headers.set("X-Request-ID", auditLog.getTraceId());

                HttpEntity<AuditReportRequest> httpEntity = new HttpEntity<>(request, headers);

                // 发送请求
                log.debug("上报审计日志到外部系统，URL: {}, 审计ID: {}", reportUrl, auditLog.getId());
                ResponseEntity<String> response = restTemplate.exchange(
                        reportUrl,
                        HttpMethod.POST,
                        httpEntity,
                        String.class
                );

                // 解析响应
                if (response.getStatusCode() == HttpStatus.OK) {
                    AuditReportResponse reportResponse = parseResponse(response.getBody());
                    log.info("审计日志上报成功，外部审计ID: {}", reportResponse.getAuditId());
                    return true;
                } else {
                    log.warn("审计日志上报失败，HTTP状态码: {}", response.getStatusCode());
                    return false;
                }
            } catch (Exception e) {
                log.error("审计日志上报异常，审计ID: {}", auditLog.getId(), e);
                throw e;
            }
        });
    }

    /**
     * 构建上报请求
     *
     * @param auditLog 审计日志
     * @return 上报请求DTO
     */
    private AuditReportRequest buildRequest(AuditLog auditLog) {
        // 解析JSON数据
        Map<String, Object> beforeData = parseJson(auditLog.getBeforeData());
        Map<String, Object> afterData = parseJson(auditLog.getAfterData());
        java.util.List<String> changedFields = parseJsonList(auditLog.getChangedFields());

        return AuditReportRequest.builder()
                .moduleName(auditLog.getModuleName())
                .operationType(auditLog.getOperationType())
                .businessId(auditLog.getBusinessId())
                .operatorId(auditLog.getOperatorId())
                .operatorName(auditLog.getOperatorName())
                .operateTime(auditLog.getOperateTime())
                .beforeData(beforeData)
                .afterData(afterData)
                .changedFields(changedFields)
                .clientIp(auditLog.getClientIp())
                .traceId(auditLog.getTraceId())
                .build();
    }

    /**
     * 解析JSON字符串为Map
     *
     * @param json JSON字符串
     * @return Map对象
     */
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析JSON失败: {}", json, e);
            return null;
        }
    }

    /**
     * 解析JSON字符串为List
     *
     * @param json JSON字符串
     * @return List对象
     */
    private java.util.List<String> parseJsonList(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<java.util.List<String>>() {});
        } catch (Exception e) {
            log.error("解析JSON失败: {}", json, e);
            return null;
        }
    }

    /**
     * 解析响应
     *
     * @param responseBody 响应体
     * @return 响应对象
     */
    private AuditReportResponse parseResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, AuditReportResponse.class);
        } catch (Exception e) {
            log.error("解析响应失败: {}", responseBody, e);
            return AuditReportResponse.success(null);
        }
    }
}
