package com.crm.system.modules.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.common.exception.BusinessException;
import com.crm.system.modules.audit.client.AuditReportClient;
import com.crm.system.modules.audit.domain.AuditLog;
import com.crm.system.modules.audit.dto.AuditQueryRequest;
import com.crm.system.modules.audit.event.AuditEvent;
import com.crm.system.modules.audit.mapper.AuditLogMapper;
import com.crm.system.modules.audit.service.AuditService;
import com.crm.system.modules.audit.util.DiffUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计服务实现类
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditService {

    private final AuditLogMapper auditLogMapper;
    private final AuditReportClient auditReportClient;
    private final DiffUtils diffUtils;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAuditLog(AuditLog auditLog) {
        try {
            // 保存审计日志
            auditLogMapper.insert(auditLog);
            log.info("审计日志保存成功，ID: {}, 模块: {}, 操作: {}",
                    auditLog.getId(), auditLog.getModuleName(), auditLog.getOperationType());
        } catch (Exception e) {
            log.error("审计日志保存失败", e);
            // 不抛出异常，避免影响业务
        }
    }

    @Override
    @Async("auditTaskExecutor")
    public void reportAuditLogAsync(Long auditLogId) {
        try {
            // 查询审计日志
            AuditLog auditLog = auditLogMapper.selectById(auditLogId);
            if (auditLog == null) {
                log.warn("审计日志不存在，ID: {}", auditLogId);
                return;
            }

            // 检查是否已上报
            if (AuditLog.ReportStatus.SUCCESS.getCode().equals(auditLog.getReportStatus())) {
                log.info("审计日志已上报，无需重复上报，ID: {}", auditLogId);
                return;
            }

            // 上报到外部系统
            boolean success = auditReportClient.report(auditLog);

            // 更新上报状态
            AuditLog updateLog = new AuditLog();
            updateLog.setId(auditLogId);
            updateLog.setLastReportTime(auditLog.getUpdateTime());

            if (success) {
                updateLog.setReportStatus(AuditLog.ReportStatus.SUCCESS.getCode());
                updateLog.setFailReason(null);
                log.info("审计日志上报成功，ID: {}", auditLogId);
            } else {
                updateLog.setRetryCount(auditLog.getRetryCount() + 1);
                updateLog.setReportStatus(AuditLog.ReportStatus.FAILED.getCode());
                updateLog.setFailReason("上报失败，将在下次重试");
                log.warn("审计日志上报失败，ID: {}，重试次数: {}", auditLogId, updateLog.getRetryCount());
            }

            auditLogMapper.updateById(updateLog);
        } catch (Exception e) {
            log.error("审计日志上报异常，ID: {}", auditLogId, e);
            // 更新失败状态
            try {
                AuditLog updateLog = new AuditLog();
                updateLog.setId(auditLogId);
                updateLog.setRetryCount(updateLog.getRetryCount() != null ? updateLog.getRetryCount() + 1 : 1);
                updateLog.setReportStatus(AuditLog.ReportStatus.FAILED.getCode());
                updateLog.setFailReason("上报异常: " + e.getMessage());
                auditLogMapper.updateById(updateLog);
            } catch (Exception ex) {
                log.error("更新审计日志失败状态失败", ex);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryFailedAuditLogs() {
        try {
            // 查询失败的审计日志
            List<AuditLog> failedLogs = auditLogMapper.selectFailedAuditLogs(
                    AuditLog.ReportStatus.FAILED.getCode(),
                    3,  // 最大重试次数
                    100  // 每次处理100条
            );

            log.info("查询到失败的审计日志 {} 条，开始重试", failedLogs.size());

            for (AuditLog auditLog : failedLogs) {
                reportAuditLogAsync(auditLog.getId());
            }
        } catch (Exception e) {
            log.error("重试失败审计日志异常", e);
        }
    }

    @Override
    public IPage<AuditLog> queryAuditLogPage(AuditQueryRequest request) {
        // 构建分页对象
        Page<AuditLog> page = request.toPage();

        // 构建查询条件
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();

        // 模块名称
        if (request.getModuleName() != null) {
            wrapper.eq(AuditLog::getModuleName, request.getModuleName());
        }

        // 操作类型
        if (request.getOperationType() != null) {
            wrapper.eq(AuditLog::getOperationType, request.getOperationType());
        }

        // 业务数据ID
        if (request.getBusinessId() != null) {
            wrapper.eq(AuditLog::getBusinessId, request.getBusinessId());
        }

        // 操作人ID
        if (request.getOperatorId() != null) {
            wrapper.eq(AuditLog::getOperatorId, request.getOperatorId());
        }

        // 操作人姓名（模糊查询）
        if (request.getOperatorName() != null) {
            wrapper.like(AuditLog::getOperatorName, request.getOperatorName());
        }

        // 操作时间范围
        if (request.getOperateTimeStart() != null) {
            wrapper.ge(AuditLog::getOperateTime, request.getOperateTimeStart());
        }
        if (request.getOperateTimeEnd() != null) {
            wrapper.le(AuditLog::getOperateTime, request.getOperateTimeEnd());
        }

        // 上报状态
        if (request.getReportStatus() != null) {
            wrapper.eq(AuditLog::getReportStatus, request.getReportStatus());
        }

        // 排序
        if ("operateTime".equals(request.getSortField())) {
            wrapper.orderBy(true, "desc".equalsIgnoreCase(request.getSortOrder()), AuditLog::getOperateTime);
        } else {
            wrapper.orderByDesc(AuditLog::getOperateTime);
        }

        // 查询分页数据
        return auditLogMapper.selectPage(page, wrapper);
    }
}
