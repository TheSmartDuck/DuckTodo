# 后端开发 Project Rules

本文档旨在统一 DuckTodo 后端开发规范，确保代码风格一致、结构清晰、易于维护。

## 1. 核心技术栈
*   **JDK**: 21
*   **Framework**: Spring Boot 3.3.0
*   **ORM**: MyBatis-Plus
*   **Security**: Spring Security + JWT
*   **AI**: Spring AI
*   **Storage**: MinIO
*   **Database**: MySQL 8.0

## 2. 代码结构规范 (Package Structure)

项目根包: `top.smartduck.ducktodo`

```text
top.smartduck.ducktodo
├── config                      # 配置类 (Configuration)
│   └── WebMvcConfig.java       # 示例：Web 配置
├── common                      # 公共组件 (Common)
│   ├── constant                # 常量定义
│   ├── enums                   # 枚举定义
│   ├── exception               # 异常处理 (Global Exception Handler)
│   └── result                  # 统一返回结果 (R)
├── controller                  # 控制层 (Controller)
├── service                     # 业务逻辑层接口 (Service Interface)
│   └── impl                    # 业务逻辑层实现 (Service Implementation)
├── mapper                      # 数据访问层 (Mapper)
├── model                       # 数据模型 (Model)
│   ├── entity                  # 数据库实体 (Entity)
│   ├── dto                     # 数据传输对象 (DTO)
│   ├── request                 # 请求对象 (Request VO)
│   └── response                # 响应对象 (Response VO)
├── util                        # 工具类 (Utils)
├── interceptor                 # 拦截器 (Interceptor)
├── filter                      # 过滤器 (Filter)
├── aspect                      # 切面 (Aspect)
├── job                         # 定时任务 (Scheduled Tasks)
├── ai                          # AI 服务 (LLM Integration)
└── DuckTodoApplication.java    # 启动类
```

| 包名 (Package) | 描述 (Description) | 规范 (Rules) |
| :--- | :--- | :--- |
| `config` | 配置类 | 存放 `@Configuration` 类 (如 Security, Swagger, MyBatis 配置)。 |
| `common` | 公共组件 | 存放公共模块。 |
| `common.exception` | 异常处理 | 存放自定义异常及全局异常处理器 (`GlobalExceptionHandler`)。 |
| `common.result` | 统一返回结果 | 存放统一响应对象 (`R`)。 |
| `common.constant` | 常量定义 | 存放全局常量。 |
| `common.enums` | 枚举定义 | 存放全局枚举。 |
| `interceptor` | 拦截器 | 存放 HandlerInterceptor 实现类。 |
| `filter` | 过滤器 | 存放 Filter 实现类。 |
| `controller` | 控制层 | 仅处理 HTTP 请求/响应，参数校验。**禁止编写业务逻辑**。 |
| `service` | 业务逻辑层 | 定义业务方法接口。 |
| `service.impl` | 业务逻辑实现 | 实现业务逻辑。**事务控制 (`@Transactional`) 应在此层**。 |
| `mapper` | 数据访问层 | 继承 `BaseMapper<T>`，存放自定义 SQL。 |
| `model` | 数据模型 | 存放所有数据对象。 |
| `model.entity` | 实体类 | 与数据库表一一对应 (`@TableName`)。使用 Lombok `@Data`。 |
| `model.dto` | 数据传输对象 | 用于 Service 层间的数据传输。 |
| `model.response` | 响应对象 | 用于 Controller 层的出参。**禁止直接返回 Entity**。 |
| `model.request` | 请求对象 | 用于 Controller 层的入参。 |
| `util` | 工具类 | 存放静态工具方法 (如 `JwtUtil`, `MinioUtil`)。 |
| `aspect` | 切面类 | 存放 AOP 切面 (如日志切面)。 |
| `job` | 定时任务 | 存放 `@Scheduled` 定时任务类。 |
| `ai` | AI 服务 | 存放 LLM 调用相关的 Service, Client 或 Tool。 |
| `DuckTodoApplication` | 启动类 | Spring Boot 启动入口。 |

## 3. 开发规范 (Development Rules)

### 3.1 命名规范
*   **类名**: `UpperCamelCase` (e.g., `TaskController`)
*   **方法/变量**: `lowerCamelCase` (e.g., `getUserById`)
*   **常量**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`)
*   **数据库表**: `snake_case` (e.g., `task_group`)
*   **接口实现类**: 必须以 `Impl` 结尾 (e.g., `TaskServiceImpl`)

### 3.2 接口规范 (API Rules)
*   **RESTful 风格**:
    *   `GET /tasks/{id}` (获取)
    *   `POST /tasks` (创建)
    *   `PUT /tasks` (更新)
    *   `DELETE /tasks/{id}` (删除)
*   **统一响应格式**: 所有接口必须返回 `R<T>` 对象。
    ```java
    // 成功
    return R.ok(data);
    // 失败
    return R.failed("错误信息");
    ```
*   **参数校验**: 使用 JSR-303 注解 (`@NotNull`, `@Size` 等) 在 VO 类中定义，Controller 方法需加 `@Valid`。

### 3.3 数据库规范 (Database Rules)
*   **主键**: 统一使用 UUID (`varchar(128)`).
*   **逻辑删除**: 所有表必须包含 `is_delete` 字段，使用 MyBatis-Plus `@TableLogic`。
*   **审计字段**: 所有表应包含 `create_time`, `update_time`。
*   **SQL 规范**:
    *   优先使用 MyBatis-Plus 的 `LambdaQueryWrapper`。
    *   复杂 SQL 必须写在 Mapper XML 中，**禁止在 Java 代码中拼接 SQL**。

### 3.4 异常处理 (Exception Handling)
*   **业务异常**: 抛出自定义异常 (e.g., `BusinessException`)，由全局异常处理器统一捕获。
*   **禁止**: 禁止在 Controller 中 `try-catch` 后吞掉异常，应抛出或返回错误 R 对象。

### 3.5 注释规范 (JavaDoc)
*   **类/接口**: 必须添加类注释，说明用途、作者 (`@author`)。
*   **方法**: public 方法必须添加 JavaDoc，说明参数 (`@param`)、返回值 (`@return`) 及异常。
*   **复杂逻辑**: 方法内部复杂逻辑需添加单行注释 (`//`) 说明步骤。

## 4. 安全规范 (Security Rules)
*   **敏感数据**: 密码、AK/SK 等敏感字段入库前必须加密 (Argon2/AES)。
*   **鉴权**: Controller 必须使用 `@PreAuthorize` 或配置类控制访问权限。
*   **SQL 注入**: 严禁使用 `${}` 拼接参数，必须使用 `#{}` 预编译。

## 5. Git 提交规范 (Commit Message)
格式: `<type>(<scope>): <subject>`
*   `feat`: 新功能
*   `fix`: 修复 Bug
*   `docs`: 文档变更
*   `style`: 代码格式调整 (不影响逻辑)
*   `refactor`: 重构 (无新功能/无 Bug 修复)
*   `chore`: 构建/工具链变动

Example: `feat(task): add audit log support`
