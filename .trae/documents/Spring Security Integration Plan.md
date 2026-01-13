我将实现 Spring Security + JWT 的集成，并重构认证逻辑以适配新的数据库结构（`User` + `UserSecurity`）。

### 1. 安全配置 (Security Configuration)
*   **创建 `SecurityConfig`**:
    *   配置 `SecurityFilterChain`：禁用 CSRF，设置会话策略为无状态 (`STATELESS`)。
    *   配置放行规则：允许匿名访问 `/api/base/**` 和 Swagger UI 相关接口。
    *   配置密码编码器：使用 `Argon2PasswordEncoder` 替代旧的 MD5。
    *   暴露 `AuthenticationManager` Bean 供登录接口使用。

### 2. 用户认证逻辑 (User Authentication Logic)
*   **创建 `LoginUserDto`**: 实现 `UserDetails` 接口，封装 `User` 实体和来自 `UserSecurity` 的密码信息，作为中间数据传输对象。
*   **创建 `UserDetailsServiceImpl`**: 实现 `UserDetailsService` 接口。通过 `UserService` 查询基本信息，通过 `UserSecurityService` 查询凭证信息。
*   **创建 `JwtAuthenticationTokenFilter`**:
    *   拦截请求，从 `Authorization` 头中提取 JWT。
    *   使用 `JwtUtil` 校验 Token。
    *   验证通过后，将 `Authentication` 对象存入 `SecurityContextHolder`。
    *   同时设置 `currentUser` 请求属性，以兼容现有代码。

### 3. 重构控制器 (Refactor Controllers)
*   **`BaseController` (登录/注册)**:
    *   **登录**: 使用 `AuthenticationManager.authenticate()` 进行标准认证，替代手动密码比对。
    *   **注册**: 使用 `PasswordEncoder.encode()` (Argon2) 加密密码。在事务中同时创建 `User` 和 `UserSecurity` 记录。
*   **`UserController`**:
    *   **修改密码**: 使用 `PasswordEncoder` 验证旧密码并加密新密码。更新 `UserSecurity` 表。
    *   **Access Keys 管理**: 修改相关接口以读取/更新 `UserSecurity` 表中的 AK/SK。
    *   **当前用户获取**: 确保 `CommonUtil.getCurrentUser()` 能正常工作（依赖 Filter 注入的属性）。

### 4. 清理与适配 (Cleanup)
*   **移除 `AuthInterceptor`**: 删除旧的手动拦截器，由 Spring Security 过滤器链接管。
*   **更新 `WebMvcConfig`**: 移除拦截器注册代码。
*   **更新 `CommonUtil`**: 确保工具类与新的加密方式兼容。

### 5. 依赖说明
*   项目已包含 `spring-boot-starter-security` 和 `bouncycastle`，可直接使用 Argon2 算法。

该计划确保了符合 Spring Security 标准的安全实现，并完美适配了用户与安全信息分离的数据库设计。
