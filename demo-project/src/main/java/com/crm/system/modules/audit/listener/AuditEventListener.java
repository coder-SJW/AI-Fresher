package com.crm.system.modules.audit.listener;

import com.crm.system.modules.audit.domain.AuditLog;
import com.crm.system.modules.audit.event.AuditEvent;
import com.crm.system.modules.audit.service.AuditService;
import com.crm.system.modules.audit.util.DiffUtils;
import com.crm.system.modules.audit.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计事件监听器
 *
 * <p>监听审计事件，异步处理审计日志的保存和上报</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditService auditService;
    private final DiffUtils diffUtils;
    private final SecurityUtils securityUtils;

    /**
     * 监听审计事件，异步处理
     *
     * <p>使用@TransactionalEventListener确保在事务提交后再处理</p>
     *
     * @param event 审计事件
     */
    @Async("auditTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuditEvent(AuditEvent event) {
        try {
            log.debug("开始处理审计事件，模块: {}, 操作: {}, 业务ID: {}",
                    event.getModuleName(), event.getOperationType(), event.getBusinessId());

            // 构建审计日志实体
            AuditLog auditLog = buildAuditLog(event);

            // 保存到数据库
            auditService.saveAuditLog(auditLog);

            // 异步上报到外部系统
            auditService.reportAuditLogAsync(auditLog.getId());

            log.debug("审计事件处理完成，审计日志ID: {}", auditLog.getId());
        } catch (Exception e) {
            log.error("审计事件处理失败", e);
            // 不抛出异常，避免影响业务
        }
    }

    /**
     * 构建审计日志实体
     *
     * @param event 审计事件
     * @return 审计日志实体
     */
    private AuditLog buildAuditLog(AuditEvent event) {
        // 计算变更字段列表（仅UPDATE操作）
        String changedFieldsJson = null;
        if (event.getOperationType() == AuditEvent.OperationType.UPDATE
                && event.getBeforeData() != null && event.getAfterData() != null) {
            List<String> changedFields = diffUtils.diffFields(event.getBeforeData(), event.getAfterData());
            changedFieldsJson = diffUtils.toJsonString(changedFields);
        }

        // 序列化数据为JSON
        String beforeDataJson = diffUtils.toJsonString(event.getBeforeData());
        String afterDataJson = diffUtils.toJsonString(event.getAfterData());

        // 获取客户端信息
        String clientIp = null;
        String traceId = null;
        if (event.getRequest() != null) {
            clientIp = securityUtils.getClientIp();
            traceId = securityUtils.generateTraceId();
        }

        return AuditLog.builder()
                .moduleName(event.getModuleName())
                .operationType(event.getOperationType().name())
                .businessId(event.getBusinessId())
                .operatorId(event.getOperator() != null ? event.getOperator().getUserId() : null)
                .operatorName(event.getOperator() != null ? event.getOperator().getUsername() : null)
                .operateTime(LocalDateTime.now())
                .beforeData(beforeDataJson)
                .afterData(afterDataJson)
                .changedFields(changedFieldsJson)
                .reportStatus(AuditLog.ReportStatus.PENDING.getCode())
                .retryCount(0)
                .clientIp(clientIp)
                .traceId(traceId)
                .build();
    }
}
