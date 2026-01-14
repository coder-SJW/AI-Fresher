package com.crm.system.modules.audit.annotation;

import com.crm.system.modules.audit.event.AuditEvent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 *
 * <p>标注在Service层方法上，自动拦截并记录操作审计日志</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * @AuditLog(
 *     moduleName = "customer",
 *     operationType = OperationType.UPDATE,
 *     businessIdParam = "id"
 * )
 * public CustomerResponse updateCustomer(Long id, CustomerCreateRequest request) {
 *     // 业务逻辑
 * }
 * }</pre>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 模块名称
     * <p>如：customer、opportunity、follow等</p>
     *
     * @return 模块名称
     */
    String moduleName() default "";

    /**
     * 操作类型
     *
     * @return 操作类型枚举
     */
    AuditEvent.OperationType operationType();

    /**
     * 业务ID参数名
     * <p>从方法参数中获取业务数据ID</p>
     *
     * @return 参数名称
     */
    String businessIdParam() default "id";

    /**
     * 是否获取变更前数据
     * <p>对于UPDATE和DELETE操作，需要获取变更前的数据</p>
     *
     * @return 是否获取变更前数据
     */
    boolean fetchBeforeData() default true;
}
