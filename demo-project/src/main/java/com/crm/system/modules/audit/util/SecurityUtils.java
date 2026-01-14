package com.crm.system.modules.audit.util;

import com.crm.system.common.security.JwtUtil;
import com.crm.system.modules.audit.event.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 安全工具类
 *
 * <p>用于获取当前操作人信息、客户端IP等安全相关信息</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
public class SecurityUtils {

    /**
     * 从当前请求中获取操作人信息
     *
     * @return 操作人信息
     */
    public AuditEvent.OperatorInfo getCurrentOperator() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String token = extractToken(request);

            if (token != null && JwtUtil.validateToken(token)) {
                Long userId = JwtUtil.getUserIdFromToken(token);
                String username = JwtUtil.getUsernameFromToken(token);

                return AuditEvent.OperatorInfo.builder()
                        .userId(userId)
                        .username(username)
                        .build();
            }
        } catch (Exception e) {
            log.warn("获取当前操作人信息失败", e);
        }

        // 返回系统用户作为默认值
        return AuditEvent.OperatorInfo.builder()
                .userId(0L)
                .username("SYSTEM")
                .build();
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    public String getClientIp() {
        HttpServletRequest request = getCurrentRequest();

        // 优先从X-Forwarded-For获取（代理情况）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个才是真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            }
            return ip;
        }

        // 从X-Real-IP获取
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 直接从RemoteAddr获取
        ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 生成请求追踪ID
     *
     * @return 追踪ID
     */
    public String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前HTTP请求
     *
     * @return HttpServletRequest
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("无法获取当前HTTP请求上下文");
        }
        return attributes.getRequest();
    }

    /**
     * 从请求中提取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}
