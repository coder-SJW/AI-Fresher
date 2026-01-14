---
name: sjw-feature-code
description: 本技能用于根据功能设计文档进行编码实现，生成完整可运行的 Spring Boot 后端代码。当用户要求"基于设计文档编码"、"实现这个功能"、"生成后端代码"或需要将设计转化为可执行代码时，使用此技能。此技能将功能设计文档转化为包含实体类、Repository、Service、Controller、DTO、数据库脚本和测试代码的完整实现。
---

# SJW 功能编码实现

## 概述

本技能提供了一套系统化的 Spring Boot 后端编码工作流程，用于将功能设计文档转化为完整可运行的代码实现。使用此技能可以生成包含实体类、数据访问层、业务逻辑层、控制器层、数据传输对象、数据库迁移脚本和完整测试套件的高质量 Spring Boot 代码，确保代码遵循最佳实践和项目规范。

## 工作流程决策树

```
开始编码实现
    │
    ├─ 是否有功能设计文档？
    │   ├─ 是 → 基于设计文档进行编码
    │   └─ 否 → 先使用功能设计技能生成设计文档
    │
    ├─ 是否有现有项目？
    │   ├─ 是 → 分析现有代码结构和规范
    │   │       ├─ 探索项目结构
    │   │       ├─ 了解现有包结构
    │   │       ├─ 分析代码风格和约定
    │   │       └─ 识别可复用组件
    │   └─ 否 → 创建新的 Spring Boot 项目结构
    │
    ├─ 编码实现范围
    │   ├─ 数据库层（Entity + Migration）
    │   ├─ 数据访问层（Repository）
    │   ├─ 业务逻辑层（Service）
    │   ├─ 控制器层（Controller + DTO）
    │   ├─ 异常处理和验证
    │   └─ 测试代码（Unit + Integration）
    │
    └─ 输出内容
        ├─ 完整的 Java 代码文件
        ├─ 数据库迁移脚本
        ├─ 测试类
        └─ 影响范围分析文档
```

## 编码工作流程

### 步骤 1：设计文档分析与项目上下文理解

在开始编码之前，深入理解设计文档和项目上下文：

1. **设计文档分析**
   - 阅读功能设计文档，理解功能需求和业务逻辑
   - 识别数据模型、API 接口、业务流程
   - 理解非功能性需求（性能、安全、事务等）
   - 提取关键技术点和实现细节

2. **项目上下文探索**（如果是现有项目）
   - 探索项目结构和包组织方式
   - 分析现有代码的风格和命名约定
   - 识别项目使用的框架和依赖（Spring Boot 版本、JPA、MyBatis 等）
   - 了解现有的异常处理、日志、配置等模式
   - 查找可复用的基类、工具类、公共组件

3. **技术栈确认**
   - Spring Boot 版本
   - 持久化框架（Spring Data JPA、MyBatis、JDBC 等）
   - 数据库类型（MySQL、PostgreSQL、H2 等）
   - 构建工具（Maven、Gradle）
   - 其他依赖（Lombok、MapStruct、Validation 等）

### 步骤 2：数据库层实现

根据数据模型设计实现数据库相关代码：

**Entity 实体类：**

```java
package com.example.project.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Comment;

/**
 * [实体描述]
 */
@Entity
@Table(name = "[table_name]", indexes = {
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class [EntityName] {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    @Comment("名称")
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**数据库迁移脚本（Flyway/Liquibase）：**

```sql
-- V1__create_[table_name].sql
CREATE TABLE [table_name] (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(255) NOT NULL COMMENT '名称',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='[表注释]';
```

### 步骤 3：数据访问层实现

实现 Repository 接口：

```java
package com.example.project.repository;

import com.example.project.entity.[EntityName];
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface [EntityName]Repository extends JpaRepository<[EntityName], Long> {

    /**
     * 根据名称查询
     */
    Optional<[EntityName]> findByName(String name);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 自定义查询
     */
    @Query("SELECT e FROM [EntityName] e WHERE e.createdAt > :date")
    List<[EntityName]> findByCreatedAtAfter(@Param("date") LocalDateTime date);
}
```

### 步骤 4：数据传输对象（DTO）实现

实现请求和响应 DTO：

**请求 DTO：**

```java
package com.example.project.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * [DTO描述]
 */
@Data
public class [EntityName]CreateRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过255个字符")
    private String name;
}
```

**响应 DTO：**

```java
package com.example.project.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * [DTO描述]
 */
@Data
@Builder
public class [EntityName]Response {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**DTO 转换工具（MapStruct）：**

```java
package com.example.project.mapper;

import com.example.project.dto.request.[EntityName]CreateRequest;
import com.example.project.dto.response.[EntityName]Response;
import com.example.project.entity.[EntityName];
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface [EntityName]Mapper {

    [EntityName] toEntity([EntityName]CreateRequest request);

    [EntityName]Response toResponse([EntityName] entity);

    void updateEntityFromRequest([EntityName]CreateRequest request,
                                  @MappingTarget [EntityName] entity);
}
```

### 步骤 5：业务逻辑层实现

实现 Service 业务逻辑：

```java
package com.example.project.service;

import com.example.project.dto.request.[EntityName]CreateRequest;
import com.example.project.dto.response.[EntityName]Response;
import com.example.entity.[EntityName];
import com.example.repository.[EntityName]Repository;
import com.example.mapper.[EntityName]Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * [Service描述]
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class [EntityName]Service {

    private final [EntityName]Repository repository;
    private final [EntityName]Mapper mapper;

    /**
     * 创建[实体名称]
     */
    @Transactional
    public [EntityName]Response create([EntityName]CreateRequest request) {
        // 业务验证
        if (repository.existsByName(request.getName())) {
            throw new BusinessException("名称已存在");
        }

        // 创建实体
        [EntityName] entity = mapper.toEntity(request);
        entity = repository.save(entity);

        return mapper.toResponse(entity);
    }

    /**
     * 根据ID查询
     */
    public [EntityName]Response getById(Long id) {
        [EntityName] entity = findByIdOrThrow(id);
        return mapper.toResponse(entity);
    }

    /**
     * 分页查询
     */
    public Page<[EntityName]Response> getPage(Pageable pageable) {
        return repository.findAll(pageable)
            .map(mapper::toResponse);
    }

    /**
     * 更新[实体名称]
     */
    @Transactional
    public [EntityName]Response update(Long id, [EntityName]CreateRequest request) {
        [EntityName] entity = findByIdOrThrow(id);

        // 业务验证
        if (!entity.getName().equals(request.getName())
            && repository.existsByName(request.getName())) {
            throw new BusinessException("名称已存在");
        }

        // 更新实体
        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);

        return mapper.toResponse(entity);
    }

    /**
     * 删除[实体名称]
     */
    @Transactional
    public void delete(Long id) {
        [EntityName] entity = findByIdOrThrow(id);
        repository.delete(entity);
    }

    /**
     * 根据ID查询实体，不存在则抛出异常
     */
    private [EntityName] findByIdOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("[实体名称]不存在"));
    }
}
```

### 步骤 6：控制器层实现

实现 Controller 接口：

```java
package com.example.project.controller;

import com.example.project.dto.request.[EntityName]CreateRequest;
import com.example.project.dto.response.[EntityName]Response;
import com.example.project.service.[EntityName]Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * [Controller描述]
 */
@RestController
@RequestMapping("/api/v1/[entity-names]")
@RequiredArgsConstructor
public class [EntityName]Controller {

    private final [EntityName]Service service;

    /**
     * 创建[实体名称]
     */
    @PostMapping
    public ResponseEntity<[EntityName]Response> create(
            @Valid @RequestBody [EntityName]CreateRequest request) {
        [EntityName]Response response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public ResponseEntity<[EntityName]Response> getById(@PathVariable Long id) {
        [EntityName]Response response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 分页查询
     */
    @GetMapping
    public ResponseEntity Page<[EntityName]Response> getPage(Pageable pageable) {
        Page<[EntityName]Response> page = service.getPage(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * 更新[实体名称]
     */
    @PutMapping("/{id}")
    public ResponseEntity<[EntityName]Response> update(
            @PathVariable Long id,
            @Valid @RequestBody [EntityName]CreateRequest request) {
        [EntityName]Response response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除[实体名称]
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 步骤 7：异常处理和全局配置

实现异常处理：

```java
package com.example.project.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}

@Getter
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
```

**全局异常处理器：**

```java
package com.example.project.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getCode(), e.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        log.error("资源未找到：{}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", e.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("参数校验失败：{}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_ERROR", errors.toString(), LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("SYSTEM_ERROR", "系统内部错误", LocalDateTime.now()));
    }

    record ErrorResponse(String code, String message, LocalDateTime timestamp) {}
}
```

### 步骤 8：测试代码实现

实现单元测试和集成测试：

**Repository 测试：**

```java
package com.example.project.repository;

import com.example.project.entity.[EntityName];
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class [EntityName]RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private [EntityName]Repository repository;

    @Test
    void shouldFindByName() {
        // Given
        [EntityName] entity = [EntityName].builder()
            .name("测试名称")
            .build();
        entityManager.persist(entity);
        entityManager.flush();

        // When
        Optional<[EntityName]> found = repository.findByName("测试名称");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("测试名称");
    }
}
```

**Service 测试：**

```java
package com.example.project.service;

import com.example.project.dto.request.[EntityName]CreateRequest;
import com.example.project.dto.response.[EntityName]Response;
import com.example.project.entity.[EntityName];
import com.example.project.exception.BusinessException;
import com.example.project.repository.[EntityName]Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class [EntityName]ServiceTest {

    @Mock
    private [EntityName]Repository repository;

    @InjectMocks
    private [EntityName]Service service;

    @Test
    void shouldCreate[EntityName]() {
        // Given
        [EntityName]CreateRequest request = new [EntityName]CreateRequest();
        request.setName("测试名称");

        [EntityName] entity = [EntityName].builder()
            .id(1L)
            .name("测试名称")
            .build();

        when(repository.existsByName("测试名称")).thenReturn(false);
        when(repository.save(any([EntityName].class))).thenReturn(entity);

        // When
        [EntityName]Response response = service.create(request);

        // Then
        assertThat(response.getName()).isEqualTo("测试名称");
        verify(repository).save(any([EntityName].class));
    }

    @Test
    void shouldThrowExceptionWhenNameExists() {
        // Given
        [EntityName]CreateRequest request = new [EntityName]CreateRequest();
        request.setName("已存在名称");

        when(repository.existsByName("已存在名称")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("名称已存在");
    }
}
```

**Controller 集成测试：**

```java
package com.example.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class [EntityName]ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreate[EntityName]() throws Exception {
        String requestBody = """
            {
                "name": "测试名称"
            }
            """;

        mockMvc.perform(post("/api/v1/[entity-names]")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("测试名称"));
    }
}
```

### 步骤 9：代码组织与影响范围分析

**代码组织结构：**

```
src/main/java/com/example/project/
├── controller/          # 控制器层
│   └── [EntityName]Controller.java
├── service/             # 业务逻辑层
│   ├── [EntityName]Service.java
│   └── impl/            # 可选：实现类
├── repository/          # 数据访问层
│   └── [EntityName]Repository.java
├── entity/              # 实体类
│   └── [EntityName].java
├── dto/                 # 数据传输对象
│   ├── request/
│   │   └── [EntityName]CreateRequest.java
│   └── response/
│       └── [EntityName]Response.java
├── mapper/              # 对象映射
│   └── [EntityName]Mapper.java
└── exception/           # 异常处理
    ├── BusinessException.java
    └── NotFoundException.java

src/main/resources/
└── db/migration/        # 数据库迁移脚本
    └── V1__create_[table_name].sql

src/test/java/com/example/project/
├── controller/          # 控制器测试
├── service/             # 服务层测试
└── repository/          # Repository 测试
```

**影响范围分析文档：**

```markdown
# 功能实现影响范围分析

## 新增文件

### 实体类
- `entity/[EntityName].java`

### 数据访问层
- `repository/[EntityName]Repository.java`

### 业务逻辑层
- `service/[EntityName]Service.java`

### 控制器层
- `controller/[EntityName]Controller.java`

### DTO 类
- `dto/request/[EntityName]CreateRequest.java`
- `dto/response/[EntityName]Response.java`

### Mapper 类
- `mapper/[EntityName]Mapper.java`

### 异常类
- `exception/[CustomException].java`（如有新增）

### 数据库脚本
- `resources/db/migration/V1__create_[table_name].sql`

### 测试类
- `repository/[EntityName]RepositoryTest.java`
- `service/[EntityName]ServiceTest.java`
- `controller/[EntityName]ControllerTest.java`

## 修改文件

### 配置文件
- `application.yml` - [如有新配置添加]

### 全局配置
- `GlobalExceptionHandler.java` - [如有新增异常处理]

### 依赖配置
- `pom.xml` / `build.gradle` - [如有新增依赖]

## 影响评估

### 数据库影响
- 新增表：`[table_name]`
- 索引影响：[新增索引说明]
- 性能影响：[性能评估]

### API 影响
- 新增端点：
  - POST `/api/v1/[entity-names]` - 创建
  - GET `/api/v1/[entity-names]/{id}` - 查询
  - GET `/api/v1/[entity-names]` - 列表
  - PUT `/api/v1/[entity-names]/{id}` - 更新
  - DELETE `/api/v1/[entity-names]/{id}` - 删除

### 系统集成影响
- 对其他模块的影响：[说明]
- 需要协调的团队：[说明]
- 上线依赖：[说明]

### 兼容性影响
- 向后兼容性：[是否兼容]
- 数据迁移需求：[是否需要]
- 配置变更需求：[是否需要]

## 部署检查清单

- [ ] 所有新文件已添加到版本控制
- [ ] 数据库迁移脚本已测试
- [ ] 所有测试用例通过
- [ ] 代码审查已完成
- [ ] API 文档已更新
- [ ] 性能测试已通过（如需要）
- [ ] 安全审查已完成（如需要）
```

### 步骤 10：代码质量检查清单

在提交代码前，验证：

**代码质量：**
- [ ] 遵循项目代码规范和命名约定
- [ ] 适当的注释和文档
- [ ] 没有硬编码的魔法值
- [ ] 适当的异常处理和错误消息
- [ ] 日志记录在关键位置

**功能完整性：**
- [ ] 实现了所有设计文档中的功能点
- [ ] CRUD 操作完整
- [ ] 业务验证逻辑完整
- [ ] 边界条件已处理

**测试覆盖：**
- [ ] 单元测试覆盖关键业务逻辑
- [ ] 集成测试覆盖 API 端点
- [ ] 异常场景测试完整
- [ ] 测试覆盖率达到项目要求（通常 > 80%）

**性能和安全：**
- [ ] N+1 查询问题已避免
- [ ] 适当的数据库索引
- [ ] 输入验证完整
- [ ] 敏感数据已保护
- [ ] 事务边界正确

**Spring Boot 最佳实践：**
- [ ] 使用 @Transactional 正确管理事务
- [ ] DTO 与 Entity 分离
- [ ] 使用 Spring Validation 进行参数校验
- [ ] 统一的异常处理机制
- [ ] 合理的分层架构
- [ ] 依赖注入使用构造函数注入

## 编码规范

**命名约定：**
- 类名：PascalCase（如：`UserService`）
- 方法名：camelCase（如：`getUserById`）
- 常量：UPPER_SNAKE_CASE（如：`MAX_SIZE`）
- 包名：全小写（如：`com.example.service`）

**代码组织：**
- 每个类只负责一个职责
- 方法不超过 50 行
- 类不超过 500 行
- 使用包结构按层组织代码

**注释规范：**
- 公共 API 必须有 Javadoc 注释
- 复杂业务逻辑必须有解释注释
- 避免无意义的注释
- 注释解释"为什么"而不是"是什么"

**最佳实践：**
- 优先使用依赖注入而非 new 对象
- 使用 Lombok 减少样板代码
- 使用 Stream API 简化集合操作
- 使用 Optional 处理可能为空的值
- 使用 @Transactional 确保数据一致性

## 常见问题和解决方案

**N+1 查询问题：**
- 使用 @EntityGraph 或 JOIN FETCH 解决
- 使用 @Query 自定义查询语句
- 使用投影（Projection）减少数据加载

**循环依赖问题：**
- 重新设计模块边界
- 使用 @Lazy 延迟加载
- 使用事件驱动模式解耦

**事务失效问题：**
- 避免在同类内部方法调用
- 使用 @Transactional(readonly = true) 优化查询
- 正确设置事务传播行为

**性能优化：**
- 使用缓存（@Cacheable）
- 批量操作（saveAll、deleteAll）
- 异步处理（@Async）
- 数据库索引优化

## Spring Boot 版本注意事项

**Spring Boot 3.x：**
- 使用 Jakarta EE（`jakarta.*`）而非 Java EE（`javax.*`）
- 支持 Java 17+
- 原生支持 GraalVM

**Spring Boot 2.x：**
- 使用 Java EE（`javax.*`）
- 支持 Java 8 或 11
- 更成熟的生态系统
