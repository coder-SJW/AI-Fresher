package com.crm.system.modules.audit.job;

import com.crm.system.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 审计日志重试定时任务
 *
 * <p>定时扫描上报失败的审计日志并重新上报</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditRetryJob {

    private final AuditService auditService;

    /**
     * 定时重试失败的审计日志上报
     *
     * <p>每小时执行一次</p>
     * <p>Cron表达式：0 0 * * * ? （每小时的0分0秒执行）</p>
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void retryFailedAuditLogs() {
        log.info("开始重试失败的审计日志上报");

        try {
            auditService.retryFailedAuditLogs();
            log.info("审计日志重试任务完成");
        } catch (Exception e) {
            log.error("重试审计日志上报失败", e);
        }
    }
}
