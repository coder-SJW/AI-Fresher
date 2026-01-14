package com.crm.system.modules.audit.aspect;

import com.crm.system.modules.audit.annotation.AuditLog;
import com.crm.system.modules.audit.event.AuditEvent;
import com.crm.system.modules.audit.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 审计日志AOP切面
 *
 * <p>拦截带有@AuditLog注解的方法，捕获操作并发布审计事件</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final SecurityUtils securityUtils;

    /**
     * 环绕通知：拦截带有@AuditLog注解的方法
     *
     * @param joinPoint  连接点
     * @param auditLog   审计注解
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        log.debug("审计切面拦截方法: {}.{}", method.getDeclaringClass().getSimpleName(), method.getName());

        // 获取变更前数据（UPDATE/DELETE操作）
        Object beforeData = null;
        if (auditLog.fetchBeforeData() && isUpdateOrDelete(auditLog)) {
            beforeData = fetchBeforeData(joinPoint, auditLog);
        }

        // 执行业务方法
        Object result = joinPoint.proceed();

        // 获取变更后数据
        Object afterData = fetchAfterData(result);

        // 获取业务ID
        Long businessId = getBusinessId(joinPoint, auditLog, result);

        // 获取操作人信息
        AuditEvent.OperatorInfo operator = securityUtils.getCurrentOperator();

        // 构建审计事件
        AuditEvent event = AuditEvent.builder()
                .moduleName(auditLog.moduleName())
                .operationType(auditLog.operationType())
                .businessId(businessId)
                .operator(operator)
                .beforeData(beforeData)
                .afterData(afterData)
                .request(securityUtils.getCurrentRequest())
                .build();

        // 发布审计事件（异步处理）
        eventPublisher.publishEvent(event);

        long endTime = System.currentTimeMillis();
        log.debug("审计切面处理完成，耗时: {}ms", endTime - startTime);

        return result;
    }

    /**
     * 判断是否为UPDATE或DELETE操作
     *
     * @param auditLog 审计注解
     * @return 是否为UPDATE或DELETE操作
     */
    private boolean isUpdateOrDelete(AuditLog auditLog) {
        return auditLog.operationType() == AuditEvent.OperationType.UPDATE
                || auditLog.operationType() == AuditEvent.OperationType.DELETE;
    }

    /**
     * 获取变更前数据
     * <p>这里需要根据具体的业务实现来查询原始数据</p>
     *
     * @param joinPoint 连接点
     * @param auditLog   审计注解
     * @return 变更前数据
     */
    private Object fetchBeforeData(ProceedingJoinPoint joinPoint, AuditLog auditLog) {
        // TODO: 根据业务ID查询原始数据
        // 这里可以通过注入Mapper来查询数据库
        // 例如：customerMapper.selectById(id)
        return null;
    }

    /**
     * 获取变更后数据
     *
     * @param result 方法执行结果
     * @return 变更后数据
     */
    private Object fetchAfterData(Object result) {
        // 通常方法执行结果就是变更后的数据
        return result;
    }

    /**
     * 从方法参数或返回值中获取业务ID
     *
     * @param joinPoint 连接点
     * @param auditLog   审计注解
     * @param result     方法执行结果
     * @return 业务ID
     */
    private Long getBusinessId(ProceedingJoinPoint joinPoint, AuditLog auditLog, Object result) {
        // 尝试从返回值中获取ID
        if (result != null) {
            try {
                // 使用反射获取getId()方法
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception e) {
                log.debug("无法从返回值中获取ID", e);
            }
        }

        // 尝试从方法参数中获取ID
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            // 通常第一个参数就是ID（对于UPDATE和DELETE操作）
            if (args[0] instanceof Long) {
                return (Long) args[0];
            }
        }

        log.warn("无法获取业务ID");
        return null;
    }
}
