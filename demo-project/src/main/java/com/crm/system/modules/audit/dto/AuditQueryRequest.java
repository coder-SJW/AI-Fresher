package com.crm.system.modules.audit.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志查询请求DTO
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditQueryRequest {

    /**
     * 当前页码
     */
    @Builder.Default
    private Long current = 1L;

    /**
     * 每页大小
     */
    @Builder.Default
    private Long size = 10L;

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
     * 操作人姓名（模糊查询）
     */
    private String operatorName;

    /**
     * 操作时间开始
     */
    private LocalDateTime operateTimeStart;

    /**
     * 操作时间结束
     */
    private LocalDateTime operateTimeEnd;

    /**
     * 上报状态
     */
    private Integer reportStatus;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder;

    /**
     * 转换为MyBatis-Plus的Page对象
     *
     * @return Page对象
     */
    public Page<AuditLog> toPage() {
        return new Page<>(current, size);
    }
}
