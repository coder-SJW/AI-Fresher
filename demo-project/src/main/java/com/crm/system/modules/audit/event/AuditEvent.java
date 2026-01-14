package com.crm.system.modules.audit.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletRequest;

/**
 * 审计事件
 *
 * <p>封装审计数据，在系统中传递审计信息</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 模块名称
     * <p>如：customer、opportunity、follow等</p>
     */
    private String moduleName;

    /**
     * 操作类型
     */
    private OperationType operationType;

    /**
     * 业务数据ID
     */
    private Long businessId;

    /**
     * 操作人信息
     */
    private OperatorInfo operator;

    /**
     * 变更前数据
     */
    private Object beforeData;

    /**
     * 变更后数据
     */
    private Object afterData;

    /**
     * HTTP请求上下文
     */
    private HttpServletRequest request;

    /**
     * 构造函数
     *
     * @param source 事件源
     */
    public AuditEvent(Object source) {
        super(source);
    }

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        /**
         * 创建操作
         */
        CREATE,

        /**
         * 更新操作
         */
        UPDATE,

        /**
         * 删除操作
         */
        DELETE
    }

    /**
     * 操作人信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorInfo {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 角色ID
         */
        private Long roleId;

        /**
         * 角色名称
         */
        private String roleName;
    }
}
