package com.crm.system.modules.audit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 审计重试配置
 *
 * <p>配置审计日志上报的失败重试策略</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Configuration
public class AuditRetryConfig {

    /**
     * 审计上报重试模板
     *
     * <p>重试策略：</p>
     * <ul>
     *   <li>最大重试次数：3次</li>
     *   <li>退避策略：指数退避</li>
     *   <li>初始间隔：1分钟（60秒）</li>
     *   <li>退避倍数：5.0</li>
     *   <li>最大间隔：30分钟（1800秒）</li>
     * </ul>
     *
     * @return 重试模板
     */
    @Bean
    public RetryTemplate auditRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 指数退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(60000);  // 1分钟
        backOffPolicy.setMultiplier(5.0);         // 倍数
        backOffPolicy.setMaxInterval(1800000);    // 最大30分钟

        // 简单重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
