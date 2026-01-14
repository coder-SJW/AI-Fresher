# CRM 客户管理系统 - 代码实现指南

**文档版本：** 1.0
**创建日期：** 2026-01-12
**基于设计文档：** 功能设计文档 v1.0

---

## 目录

1. [项目结构](#1-项目结构)
2. [核心组件实现](#2-核心组件实现)
3. [客户管理模块实现](#3-客户管理模块实现)
4. [其他模块实现指南](#4-其他模块实现指南)
5. [测试代码实现](#5-测试代码实现)
6. [部署和运行](#6-部署和运行)

---

## 1. 项目结构

### 1.1 完整项目结构

```
crm-system/
├── pom.xml                                    # Maven 配置文件
├── src/
│   ├── main/
│   │   ├── java/com/crm/system/
│   │   │   ├── CrmApplication.java           # 主启动类
│   │   │   ├── common/                        # 公共模块
│   │   │   │   ├── result/                    # 统一响应
│   │   │   │   │   ├── Result.java
│   │   │   │   │   └── PageResult.java
│   │   │   │   ├── exception/                 # 异常处理
│   │   │   │   │   ├── BusinessException.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── config/                    # 配置类
│   │   │   │   │   ├── MyBatisPlusConfig.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── SwaggerConfig.java
│   │   │   │   ├── security/                  # 安全模块
│   │   │   │   │   ├── JwtUtil.java
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   └── SecurityUtils.java
│   │   │   │   └── constants/                 # 常量定义
│   │   │   │       └── ErrorCode.java
│   │   │   ├── modules/                       # 业务模块
│   │   │   │   ├── customer/                  # 客户管理模块
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── service/
│   │   │   │   │   ├── mapper/
│   │   │   │   │   ├── domain/
│   │   │   │   │   └── dto/
│   │   │   │   ├── followup/                  # 跟进记录模块
│   │   │   │   ├── opportunity/               # 销售机会模块
│   │   │   │   └── system/                    # 系统管理模块
│   │   │   │       ├── user/
│   │   │   │       ├── role/
│   │   │   │       └── dict/
│   │   │   └── utils/                         # 工具类
│   │   └── resources/
│   │       ├── application.yml                # 应用配置
│   │       ├── db/migration/                  # 数据库迁移脚本
│   │       │   └── V1__init_schema.sql
│   │       └── mapper/                        # MyBatis XML 映射文件
│   └── test/                                  # 测试代码
│       └── java/com/crm/system/
└── README.md                                  # 项目说明文档
```

---

## 2. 核心组件实现

### 2.1 主启动类

**文件：** `CrmApplication.java`

```java
package com.crm.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CRM 系统主启动类
 */
@SpringBootApplication
@MapperScan("com.crm.system.**.mapper")
public class CrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("CRM 系统启动成功！");
        System.out.println("API 文档地址: http://localhost:8080/api/doc.html");
        System.out.println("========================================\n");
    }
}
```

---

### 2.2 MyBatis-Plus 配置

**文件：** `MyBatisPlusConfig.java`

```java
package com.crm.system.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器
     * 配置分页插件和乐观锁插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 设置单页最大限制数量
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }
}
```

---

### 2.3 JWT 工具类

**文件：** `JwtUtil.java`

```java
package com.crm.system.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成 Token
     */
    public String generateToken(Long userId, String username, Long roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleId", roleId);
        return generateToken(claims);
    }

    /**
     * 生成 Token
     */
    private String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从 Token 中获取角色 ID
     */
    public Long getRoleIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("roleId").toString());
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token 验证失败", e);
            return false;
        }
    }

    /**
     * 从 Token 中获取 Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 判断 Token 是否过期
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }
}
```

---

## 3. 客户管理模块实现

### 3.1 实体类

**文件：** `Customer.java`

```java
package com.crm.system.modules.customer.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("crm_customer")
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 公司地址
     */
    private String companyAddress;

    /**
     * 客户来源
     */
    private String customerSource;

    /**
     * 客户状态
     */
    private String customerStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记（0:正常 1:删除）
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
```

---

### 3.2 Mapper 接口

**文件：** `CustomerMapper.java`

```java
package com.crm.system.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.modules.customer.domain.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 客户 Mapper 接口
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    // MyBatis-Plus 提供了基础的 CRUD 方法，无需额外编写

    // 如需自定义查询，可在此添加方法并使用 @Select 注解或创建 XML 文件
}
```

---

### 3.3 DTO 类

**请求 DTO：** `CustomerCreateRequest.java`

```java
package com.crm.system.modules.customer.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 客户创建请求 DTO
 */
@Data
public class CustomerCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "客户名称不能为空")
    @Size(max = 100, message = "客户名称长度不能超过100个字符")
    private String customerName;

    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 50, message = "联系人姓名长度不能超过50个字符")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    @Email(message = "邮箱格式不正确")
    private String contactEmail;

    @Size(max = 255, message = "公司地址长度不能超过255个字符")
    private String companyAddress;

    private String customerSource;

    @NotBlank(message = "客户状态不能为空")
    private String customerStatus;

    private String remark;
}
```

**响应 DTO：** `CustomerResponse.java`

```java
package com.crm.system.modules.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户响应 DTO
 */
@Data
@Builder
public class CustomerResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String customerName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String companyAddress;
    private String customerSource;
    private String customerStatus;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**查询 DTO：** `CustomerQueryRequest.java`

```java
package com.crm.system.modules.customer.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户查询请求 DTO
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
```

---

### 3.4 Service 层

**接口：** `CustomerService.java`

```java
package com.crm.system.modules.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.system.modules.customer.domain.Customer;
import com.crm.system.modules.customer.dto.*;

/**
 * 客户服务接口
 */
public interface CustomerService {

    /**
     * 创建客户
     */
    CustomerResponse createCustomer(CustomerCreateRequest request);

    /**
     * 更新客户
     */
    CustomerResponse updateCustomer(Long id, CustomerCreateRequest request);

    /**
     * 删除客户
     */
    void deleteCustomer(Long id);

    /**
     * 根据ID查询客户
     */
    CustomerResponse getCustomerById(Long id);

    /**
     * 分页查询客户
     */
    IPage<CustomerResponse> getCustomerPage(CustomerQueryRequest request);
}
```

**实现类：** `CustomerServiceImpl.java`

```java
package com.crm.system.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.common.exception.BusinessException;
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
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    private final CustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
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
```

---

### 3.5 Controller 层

**文件：** `CustomerController.java`

```java
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
 */
@Api(tags = "客户管理")
@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 创建客户
     */
    @ApiOperation("创建客户")
    @PostMapping
    public Result<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return Result.success(response);
    }

    /**
     * 更新客户
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
     */
    @ApiOperation("删除客户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }

    /**
     * 根据ID查询客户
     */
    @ApiOperation("根据ID查询客户")
    @GetMapping("/{id}")
    public Result<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return Result.success(response);
    }

    /**
     * 分页查询客户
     */
    @ApiOperation("分页查询客户")
    @GetMapping
    public Result<PageResult<CustomerResponse>> getCustomerPage(CustomerQueryRequest request) {
        IPage<CustomerResponse> page = customerService.getCustomerPage(request);
        return Result.success(PageResult.of(page));
    }
}
```

---

## 4. 其他模块实现指南

其他模块（跟进记录、销售机会、用户管理、数据字典）的实现方式与客户管理模块类似，遵循相同的模式和结构。

### 4.1 跟进记录模块

**关键点：**
- 实体类：`FollowUp.java`
- Mapper：`FollowUpMapper.java`
- Service：`FollowUpService.java`
- Controller：`FollowUpController.java`
- 关键功能：按客户查询、跟进提醒

**示例查询方法：**
```java
// 查询今天需要跟进的客户
@Select("SELECT f.*, c.customer_name, c.contact_name, c.contact_phone " +
        "FROM crm_follow_up f " +
        "LEFT JOIN crm_customer c ON f.customer_id = c.id " +
        "WHERE f.next_follow_up_time <= CURDATE() " +
        "AND f.is_deleted = 0 " +
        "ORDER BY f.next_follow_up_time ASC")
List<FollowUpReminderVO> getTodayReminders();
```

---

### 4.2 销售机会模块

**关键点：**
- 实体类：`Opportunity.java`
- Mapper：`OpportunityMapper.java`
- Service：`OpportunityService.java`
- Controller：`OpportunityController.java`
- 关键功能：销售漏斗统计

**示例统计方法：**
```java
// 销售漏斗统计
@Select("SELECT sales_stage, COUNT(*) as count, " +
        "SUM(estimated_amount) as total_amount " +
        "FROM crm_opportunity " +
        "WHERE is_deleted = 0 " +
        "GROUP BY sales_stage " +
        "ORDER BY sort_order")
List<SalesFunnelVO> getSalesFunnel();
```

---

### 4.3 用户管理模块

**关键点：**
- 实体类：`User.java`
- Mapper：`UserMapper.java`
- Service：`UserService.java`
- Controller：`UserController.java`
- 密码加密：使用 BCrypt

**密码加密示例：**
```java
// 注册用户时加密密码
String encryptedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

// 验证密码
boolean isMatch = BCrypt.checkpw(rawPassword, encryptedPassword);
```

---

### 4.4 数据字典模块

**关键点：**
- 实体类：`Dict.java`
- Mapper：`DictMapper.java`
- Service：`DictService.java`
- Controller：`DictController.java`
- 缓存：使用 Redis 或内存缓存

**缓存示例：**
```java
@Cacheable(value = "dict", key = "#dictType")
public List<Dict> getDictByType(String dictType) {
    return dictMapper.selectList(
        new LambdaQueryWrapper<Dict>()
            .eq(Dict::getDictType, dictType)
            .orderByAsc(Dict::getSortOrder)
    );
}
```

---

## 5. 测试代码实现

### 5.1 Service 层测试

**示例：** `CustomerServiceImplTest.java`

```java
package com.crm.system.modules.customer.service;

import com.crm.system.modules.customer.dto.CustomerCreateRequest;
import com.crm.system.modules.customer.dto.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 客户服务测试
 */
@SpringBootTest
@Transactional // 测试后回滚数据
class CustomerServiceImplTest {

    @Autowired
    private CustomerService customerService;

    @Test
    void testCreateCustomer() {
        // Given
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerName("测试客户");
        request.setContactName("张三");
        request.setContactPhone("13800138000");
        request.setCustomerStatus("POTENTIAL");

        // When
        CustomerResponse response = customerService.createCustomer(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCustomerName()).isEqualTo("测试客户");
        assertThat(response.getContactPhone()).isEqualTo("13800138000");
    }

    @Test
    void testGetCustomerById() {
        // Given
        Long customerId = 1L;

        // When
        CustomerResponse response = customerService.getCustomerById(customerId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(customerId);
    }
}
```

---

### 5.2 Controller 层测试

**示例：** `CustomerControllerTest.java`

```java
package com.crm.system.modules.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 客户 Controller 测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetCustomerById() throws Exception {
        mockMvc.perform(get("/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testCreateCustomer() throws Exception {
        String requestBody = """
            {
                "customerName": "测试客户",
                "contactName": "张三",
                "contactPhone": "13800138000",
                "customerStatus": "POTENTIAL"
            }
            """;

        mockMvc.perform(post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

## 6. 部署和运行

### 6.1 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE crm_db DEFAULT CHARACTER SET utf8mb4;
```

2. 执行初始化脚本：
```bash
mysql -u root -p crm_db < src/main/resources/db/migration/V1__init_schema.sql
```

3. 修改配置文件：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/crm_db
    username: your_username
    password: your_password
```

---

### 6.2 启动应用

1. Maven 打包：
```bash
mvn clean package -DskipTests
```

2. 运行应用：
```bash
java -jar target/crm-system-1.0.0.jar
```

3. 访问应用：
- API 地址：http://localhost:8080/api
- API 文档：http://localhost:8080/api/doc.html
- Druid 监控：http://localhost:8080/api/druid

---

### 6.3 默认账号

- 用户名：`admin`
- 密码：`123456`
- 角色：系统管理员

**⚠️ 重要：生产环境中请立即修改默认密码！**

---

## 7. 下一步工作

### 7.1 必须完成的功能

- [ ] 用户登录认证（JWT）
- [ ] 权限拦截器
- [ ] 所有 CRUD 接口
- [ ] 参数校验
- [ ] 异常处理

### 7.2 可选增强功能

- [ ] 数据字典缓存（Redis）
- [ ] 操作日志记录
- [ ] 文件上传功能
- [ ] 数据导出功能
- [ ] 定时任务（跟进提醒）

---

## 8. 常见问题

### Q1: 如何添加新的业务模块？

A: 按照客户管理模块的模式，创建对应的 domain、mapper、service、controller、dto 包和类。

### Q2: 如何处理事务？

A: 在 Service 方法上使用 `@Transactional` 注解：
```java
@Transactional(rollbackFor = Exception.class)
public void someMethod() {
    // 业务逻辑
}
```

### Q3: 如何实现分页查询？

A: 使用 MyBatis-Plus 的 `Page` 类：
```java
Page<Customer> page = new Page<>(current, size);
IPage<Customer> result = mapper.selectPage(page, wrapper);
```

### Q4: 如何实现乐观锁？

A: 在实体类中添加 `@Version` 注解：
```java
@Version
private Integer version;
```

---

**实现指南结束**

---

## 附录：完整文件清单

### 已创建的核心文件

✅ **配置文件**
- `pom.xml` - Maven 配置
- `application.yml` - 应用配置
- `V1__init_schema.sql` - 数据库初始化脚本

✅ **公共组件**
- `Result.java` - 统一响应
- `PageResult.java` - 分页响应
- `BusinessException.java` - 业务异常
- `GlobalExceptionHandler.java` - 全局异常处理

✅ **客户管理模块（示例）**
- `Customer.java` - 实体类
- `CustomerMapper.java` - Mapper 接口
- `CustomerCreateRequest.java` - 创建请求 DTO
- `CustomerResponse.java` - 响应 DTO
- `CustomerQueryRequest.java` - 查询请求 DTO
- `CustomerService.java` - 服务接口
- `CustomerServiceImpl.java` - 服务实现
- `CustomerController.java` - 控制器

### 待创建的文件

⬜ **核心配置**
- `MyBatisPlusConfig.java` - MyBatis-Plus 配置
- `SecurityConfig.java` - 安全配置
- `SwaggerConfig.java` - Swagger 配置

⬜ **安全模块**
- `JwtUtil.java` - JWT 工具类
- `JwtAuthenticationFilter.java` - JWT 认证过滤器
- `SecurityUtils.java` - 安全工具类

⬜ **其他业务模块**
- 跟进记录模块（FollowUp）
- 销售机会模块（Opportunity）
- 用户管理模块（User）
- 数据字典模块（Dict）

⬜ **测试代码**
- Service 层测试
- Controller 层测试

---

**开发团队请注意：**

1. **参考客户管理模块**的实现模式，快速开发其他模块
2. **遵循统一的代码规范**和命名约定
3. **编写充分的测试用例**，确保代码质量
4. **及时更新 API 文档**，使用 Swagger 注解
5. **注意事务管理**，确保数据一致性

祝开发顺利！🚀
