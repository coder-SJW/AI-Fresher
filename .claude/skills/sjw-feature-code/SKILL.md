---
name: sjw-feature-code
description: 本技能用于根据功能设计文档进行编码实现，生成完整可运行的后端代码。当用户要求"基于设计文档编码"、"实现这个功能"、"生成后端代码"或需要将设计转化为可执行代码时，使用此技能。
---

# SJW 功能编码实现

## 核心原则

- **适配项目技术栈**：检测并遵循项目现有的技术栈和代码风格
- **创建实际文件**：使用 Write 工具创建实际的代码文件
- **核心模块完整实现**：至少完整实现一个业务模块的所有层级
- **提供实现指南**：生成实现文档说明代码结构

## 工作流程

### 1. 技术栈检测

**检测项目配置文件（pom.xml 或 build.gradle）：**
- Spring Boot 版本：3.x 使用 `jakarta.*`，2.x 使用 `javax.*`
- 持久化框架：JPA、MyBatis 或 JDBC
- 构建工具：Maven 或 Gradle
- 常用库：Lombok、MapStruct、Validation、Knife4j

**分析现有代码结构：**
- 包结构和命名约定
- Result 封装模式
- 异常处理和日志模式
- 配置管理模式

### 2. 文件创建顺序

**按依赖顺序创建文件，避免编译错误：**

**第1批（无依赖）：** Entity、DTO、Enum、Util
**第2批：** Config 配置类
**第3批：** Mapper/Repository、Mapper XML
**第4批：** Service 接口和实现类
**第5批：** Controller
**第6批：** 数据库脚本、application.yml
**第7批：** 修改现有文件

### 3. 数据库层实现

**数据库脚本：** `src/main/resources/db/migration/V1__create_[table_name].sql`

**实体类：** `src/main/java/[package]/entity/[EntityName].java`

- JPA：使用 `@Entity`、`@Table`、`@Id`、`@Column`
- MyBatis：使用 `@TableName`、`@TableId`、`@TableField`
- JDBC：纯 POJO + Lombok

### 4. 数据访问层

- JPA：继承 `JpaRepository`，使用 `@Repository`
- MyBatis：使用 `@Mapper`，创建 Mapper 接口和 XML

### 5. 数据传输对象

**请求 DTO：** 使用 Validation 注解（`@NotNull`、`@NotBlank`、`@Size`）
**响应 DTO：** 精简字段，考虑使用 `@Builder`

**DTO 转换：** 手动转换、MapStruct、ModelMapper 或 Spring BeanUtils

### 6. 业务逻辑层

**Service 接口：** 定义业务方法签名
**Service 实现：**
- 使用 `@Service` 和 `@Transactional`
- 构造函数注入依赖
- 查询方法使用 `@Transactional(readOnly = true)`

### 7. 控制器层

**创建：** `src/main/java/[package]/controller/[EntityName]Controller.java`

- 使用 `@RestController` 和 `@RequestMapping`
- 遵循 RESTful 风格
- 使用 `@Valid` 进行参数校验
- 返回统一的响应格式（Result）

### 8. 异常处理和配置

**自定义异常：** BusinessException、NotFoundException、ValidationException

**全局异常处理器：** 使用 `@RestControllerAdvice`

**统一响应格式 Result：** code、message、data、timestamp

### 9. 文档输出

**当由 workflow 调用时（有参数）：**
- `DOCS_BASE_PATH` - 项目文档根目录
- `IMPLEMENTATION_PATH` - 编码实现阶段目录
- 实现指南保存到：`$IMPLEMENTATION_PATH/实现指南.md`
- 影响范围分析保存到：`$IMPLEMENTATION_PATH/影响范围分析.md`

**独立使用时（无参数）：**
- 在当前目录创建 `实现指南-[需求名称].md` 和 `影响范围分析-[需求名称].md`

**实现指南模板：**
```markdown
# 功能实现指南

## 实现概述
[简要说明实现的功能]

## 技术栈
- Spring Boot 版本
- 持久化框架

## 代码结构
[说明新增的文件和包结构]

## 核心实现说明
[说明关键实现点]

## 集成验证步骤
[说明如何验证功能]
```

**影响范围分析模板：**
```markdown
# 功能实现影响范围分析

## 新增文件
[列出所有新增的文件]

## 修改文件
[列出所有修改的文件]

## 影响评估

### 数据库影响
- 新增表：[表名]

### API 影响
- 新增端点：[列出 API]

### 系统集成影响
- 对其他模块的影响：[说明]

## 部署检查清单
- [ ] 所有新文件已添加到版本控制
- [ ] 数据库迁移脚本已测试
- [ ] 所有测试用例通过
```

### 10. 验证

**编译验证：** `mvn clean compile`
**启动验证：** `mvn spring-boot:run`
**功能验证：** 访问 API 文档，测试功能流程

## 技术栈差异速查

| 特性 | Spring Boot 3.x | Spring Boot 2.x |
|------|----------------|----------------|
| 包名 | `jakarta.*` | `javax.*` |
| Java | 17+ | 8 / 11 |

| 框架 | 注解 | 接口 |
|------|------|------|
| JPA | `@Entity`, `@Table` | 继承 `JpaRepository`, 使用 `@Repository` |
| MyBatis | `@TableName`, `@TableId` | 使用 `@Mapper`, 创建 XML |

## 编码规范

**命名：** 类名 PascalCase，方法名 camelCase，常量 UPPER_SNAKE_CASE
**组织：** 单一职责，方法不超过 50 行，类不超过 500 行
**注释：** 公共 API 必须有 Javadoc，复杂业务逻辑必须注释
**最佳实践：** 依赖注入、Lombok、Stream API、Optional、@Transactional

## 常见问题

**N+1 查询：** 使用 @EntityGraph 或 JOIN FETCH
**循环依赖：** 使用 @Lazy 延迟加载或事件驱动
**事务失效：** 避免同类内部方法调用
**性能优化：** 缓存、批量操作、异步处理、索引优化
