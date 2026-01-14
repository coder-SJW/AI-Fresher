package com.crm.system.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.common.exception.BusinessException;
import com.crm.system.modules.audit.annotation.AuditLog;
import com.crm.system.modules.audit.event.AuditEvent;
import com.crm.system.modules.customer.domain.Customer;
import com.crm.system.modules.customer.dto.*;
import com.crm.system.modules.customer.mapper.CustomerMapper;
import com.crm.system.modules.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户服务实现类
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    private final CustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            moduleName = "customer",
            operationType = AuditEvent.OperationType.CREATE,
            businessIdParam = "id",
            fetchBeforeData = false
    )
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        // 检查手机号是否已存在
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getContactPhone, request.getContactPhone());
        if (customerMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该手机号已存在");
        }

        // 创建客户实体
        Customer customer = new Customer();
        BeanUtils.copyProperties(request, customer);

        // 保存客户
        customerMapper.insert(customer);

        // 返回响应
        return buildResponse(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            moduleName = "customer",
            operationType = AuditEvent.OperationType.UPDATE,
            businessIdParam = "id",
            fetchBeforeData = true
    )
    public CustomerResponse updateCustomer(Long id, CustomerCreateRequest request) {
        // 查询客户
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        // 检查手机号是否被其他客户使用
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getContactPhone, request.getContactPhone());
        wrapper.ne(Customer::getId, id);
        if (customerMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该手机号已被其他客户使用");
        }

        // 更新客户信息
        BeanUtils.copyProperties(request, customer);
        customerMapper.updateById(customer);

        return buildResponse(customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            moduleName = "customer",
            operationType = AuditEvent.OperationType.DELETE,
            businessIdParam = "id",
            fetchBeforeData = true
    )
    public void deleteCustomer(Long id) {
        // 检查客户是否存在
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        // TODO: 检查是否有关联数据（跟进记录、销售机会）
        // 如有关联数据，抛出异常或级联删除

        // 删除客户（逻辑删除）
        customerMapper.deleteById(id);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        return buildResponse(customer);
    }

    @Override
    public IPage<CustomerResponse> getCustomerPage(CustomerQueryRequest request) {
        // 构建分页对象
        Page<Customer> page = new Page<>(request.getCurrent(), request.getSize());

        // 构建查询条件
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        // 模糊搜索
        if (StringUtils.isNotBlank(request.getCustomerName())) {
            wrapper.like(Customer::getCustomerName, request.getCustomerName());
        }
        if (StringUtils.isNotBlank(request.getContactName())) {
            wrapper.like(Customer::getContactName, request.getContactName());
        }
        if (StringUtils.isNotBlank(request.getContactPhone())) {
            wrapper.like(Customer::getContactPhone, request.getContactPhone());
        }

        // 精确筛选
        if (StringUtils.isNotBlank(request.getCustomerStatus())) {
            wrapper.eq(Customer::getCustomerStatus, request.getCustomerStatus());
        }
        if (StringUtils.isNotBlank(request.getCustomerSource())) {
            wrapper.eq(Customer::getCustomerSource, request.getCustomerSource());
        }

        // 排序
        if ("createTime".equals(request.getSortField())) {
            wrapper.orderBy(true, "desc".equalsIgnoreCase(request.getSortOrder()), Customer::getCreateTime);
        }

        // 查询分页数据
        IPage<Customer> customerPage = customerMapper.selectPage(page, wrapper);

        // 转换为响应 DTO
        return customerPage.convert(this::buildResponse);
    }

    /**
     * 构建响应 DTO
     *
     * @param customer 客户实体
     * @return 客户响应 DTO
     */
    private CustomerResponse buildResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerName(customer.getCustomerName())
                .contactName(customer.getContactName())
                .contactPhone(customer.getContactPhone())
                .contactEmail(customer.getContactEmail())
                .companyAddress(customer.getCompanyAddress())
                .customerSource(customer.getCustomerSource())
                .customerStatus(customer.getCustomerStatus())
                .remark(customer.getRemark())
                .createTime(customer.getCreateTime())
                .updateTime(customer.getUpdateTime())
                .build();
    }
}
