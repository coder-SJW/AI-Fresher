package com.crm.system.modules.audit.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数据对比工具类
 *
 * <p>用于对比两个对象的差异，识别发生变化的字段</p>
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Slf4j
@Component
public class DiffUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 需要忽略的系统字段
     */
    private static final Set<String> IGNORED_FIELDS = Set.of(
            "createTime",
            "updateTime",
            "version",
            "isDeleted",
            "createTime",
            "updateTime"
    );

    /**
     * 对比两个对象的差异
     *
     * @param before 变更前的对象
     * @param after  变更后的对象
     * @return 发生变化的字段列表
     */
    public List<String> diffFields(Object before, Object after) {
        if (before == null || after == null) {
            return Collections.emptyList();
        }

        try {
            // 将对象转换为Map
            Map<String, Object> beforeMap = convertToMap(before);
            Map<String, Object> afterMap = convertToMap(after);

            // 对比差异
            List<String> changedFields = new ArrayList<>();
            for (Map.Entry<String, Object> entry : afterMap.entrySet()) {
                String field = entry.getKey();
                Object afterValue = entry.getValue();

                // 忽略系统字段
                if (IGNORED_FIELDS.contains(field)) {
                    continue;
                }

                Object beforeValue = beforeMap.get(field);

                // 对比值是否相同
                if (!Objects.equals(beforeValue, afterValue)) {
                    changedFields.add(field);
                }
            }

            return changedFields;
        } catch (Exception e) {
            log.error("数据对比失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 将对象转换为Map
     *
     * @param obj 对象
     * @return Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object obj) {
        try {
            // 使用Jackson将对象转换为Map
            return OBJECT_MAPPER.convertValue(obj, Map.class);
        } catch (IllegalArgumentException e) {
            log.error("对象转换Map失败: {}", obj.getClass().getName(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 将对象序列化为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象序列化失败: {}", obj.getClass().getName(), e);
            return null;
        }
    }
}
