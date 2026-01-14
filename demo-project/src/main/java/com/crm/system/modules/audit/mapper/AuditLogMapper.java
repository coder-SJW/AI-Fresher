package com.crm.system.modules.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.modules.audit.domain.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 审计日志Mapper接口
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 查询待重试的审计日志
     *
     * @param reportStatus 上报状态
     * @param maxRetryCount 最大重试次数
     * @param limit 限制数量
     * @return 审计日志列表
     */
    java.util.List<AuditLog> selectFailedAuditLogs(
            @Param("reportStatus") Integer reportStatus,
            @Param("maxRetryCount") Integer maxRetryCount,
            @Param("limit") Integer limit
    );
}
