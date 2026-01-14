package com.crm.system.modules.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.system.modules.customer.dto.*;

/**
 * 客户服务接口
 *
 * @author AI Assistant
 * @since 1.0.0
 */
public interface CustomerService {

    /**
     * 创建客户
     *
     * @param request 创建请求
     * @return 客户响应
     */
    CustomerResponse createCustomer(CustomerCreateRequest request);

    /**
     * 更新客户
     *
     * @param id      客户ID
     * @param request 更新请求
     * @return 客户响应
     */
    CustomerResponse updateCustomer(Long id, CustomerCreateRequest request);

    /**
     * 删除客户
     *
     * @param id 客户ID
     */
    void deleteCustomer(Long id);

    /**
     * 根据ID查询客户
     *
     * @param id 客户ID
     * @return 客户响应
     */
    CustomerResponse getCustomerById(Long id);

    /**
     * 分页查询客户
     *
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<CustomerResponse> getCustomerPage(CustomerQueryRequest request);
}
