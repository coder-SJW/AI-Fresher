package com.crm.system.modules.customer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.system.common.result.PageResult;
import com.crm.system.common.result.Result;
import com.crm.system.modules.customer.dto.*;
import com.crm.system.modules.customer.service.CustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 客户管理 Controller
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@Api(tags = "客户管理")
@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 创建客户
     *
     * @param request 创建请求
     * @return 客户响应
     */
    @ApiOperation("创建客户")
    @PostMapping
    public Result<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return Result.success(response);
    }

    /**
     * 更新客户
     *
     * @param id      客户ID
     * @param request 更新请求
     * @return 客户响应
     */
    @ApiOperation("更新客户")
    @PutMapping("/{id}")
    public Result<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return Result.success(response);
    }

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @return 操作结果
     */
    @ApiOperation("删除客户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }

    /**
     * 根据ID查询客户
     *
     * @param id 客户ID
     * @return 客户响应
     */
    @ApiOperation("根据ID查询客户")
    @GetMapping("/{id}")
    public Result<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return Result.success(response);
    }

    /**
     * 分页查询客户
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @ApiOperation("分页查询客户")
    @GetMapping
    public Result<PageResult<CustomerResponse>> getCustomerPage(CustomerQueryRequest request) {
        IPage<CustomerResponse> page = customerService.getCustomerPage(request);
        return Result.success(PageResult.of(page));
    }
}
