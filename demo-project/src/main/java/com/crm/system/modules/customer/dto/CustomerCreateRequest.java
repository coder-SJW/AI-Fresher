package com.crm.system.modules.customer.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 客户创建请求 DTO
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Data
public class CustomerCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户名称
     */
    @NotBlank(message = "客户名称不能为空")
    @Size(max = 100, message = "客户名称长度不能超过100个字符")
    private String customerName;

    /**
     * 联系人姓名
     */
    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 50, message = "联系人姓名长度不能超过50个字符")
    private String contactName;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String contactEmail;

    /**
     * 公司地址
     */
    @Size(max = 255, message = "公司地址长度不能超过255个字符")
    private String companyAddress;

    /**
     * 客户来源
     */
    private String customerSource;

    /**
     * 客户状态
     */
    @NotBlank(message = "客户状态不能为空")
    private String customerStatus;

    /**
     * 备注
     */
    private String remark;
}
