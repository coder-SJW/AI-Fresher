package com.crm.system.modules.customer.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户查询请求 DTO
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
public class CustomerQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 客户名称（模糊搜索）
     */
    private String customerName;

    /**
     * 联系人姓名（模糊搜索）
     */
    private String contactName;

    /**
     * 联系电话（模糊搜索）
     */
    private String contactPhone;

    /**
     * 客户状态
     */
    private String customerStatus;

    /**
     * 客户来源
     */
    private String customerSource;

    /**
     * 排序字段
     */
    private String sortField = "createTime";

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder = "desc";
}
