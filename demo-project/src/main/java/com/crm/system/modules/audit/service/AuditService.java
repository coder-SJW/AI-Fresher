package com.crm.system.modules.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.system.modules.audit.domain.AuditLog;
import com.crm.system.modules.audit.dto.AuditQueryRequest;

/**
 * 审计服务接口
 *
 * @author AI Assistant
 * @since 1.0.0
 */
public interface AuditService {

    /**
     * 保存审计日志到数据库
     *
     * @param auditLog 审计日志
     */
    void saveAuditLog(AuditLog auditLog);

    /**
     * 异步上报审计日志到外部系统
     *
     * @param auditLogId 审计日志ID
     */
    void reportAuditLogAsync(Long auditLogId);

    /**
     * 批量重试失败的审计日志上报
     * <p>定时任务调用</p>
     */
    void retryFailedAuditLogs();

    /**
     * 查询审计日志分页
     *
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<AuditLog> queryAuditLogPage(AuditQueryRequest request);
}
