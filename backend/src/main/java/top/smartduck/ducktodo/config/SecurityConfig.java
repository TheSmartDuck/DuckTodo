package top.smartduck.ducktodo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.smartduck.ducktodo.filter.JwtAuthenticationTokenFilter;

/**
 * Spring Security 核心配置类
 * <p>
 * 负责配置：
 * 1. 认证过滤器链 (SecurityFilterChain)
 * 2. 密码编码器 (PasswordEncoder)
 * 3. 认证管理器 (AuthenticationManager)
 * 4. 接口访问权限控制 (AuthorizeRequests)
 * </p>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启注解权限控制，如 @PreAuthorize
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 配置安全过滤器链
     *
     * @param http HttpSecurity 对象，用于构建安全配置
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Security Filter Chain...");
        http
                // 1. 禁用 CSRF (跨站请求伪造) 保护
                // 因为我们使用 JWT 且是无状态的 REST API，不需要依赖 Session/Cookie，所以不需要 CSRF 保护
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 配置会话管理策略
                // 设置为 STATELESS (无状态)，意味着 Spring Security 不会创建或使用 HttpSession
                // 每个请求都必须携带认证信息 (如 Token)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 3.1 放行无需认证的基础接口
                        // /api/base/** 包含登录、注册、获取配置等公共接口
                        .requestMatchers("/api/base/**").permitAll()

                        // 3.2 放行 API 文档相关接口 (Knife4j / Swagger)
                        // 方便开发调试，生产环境可根据需要调整
                        .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // 3.3 放行 AI 后端代理接口（可根据需要添加认证）
                        // /api/ai/** 会被代理到 AI 后端服务
                        .requestMatchers("/api/ai/**").permitAll()

                        // 3.4 其他所有请求都需要认证 (Authenticated)
                        // 只有通过认证的用户才能访问
                        .anyRequest().authenticated()
                );

        // 4. 添加自定义 JWT 认证过滤器
        // 将 jwtAuthenticationTokenFilter 添加到 UsernamePasswordAuthenticationFilter 之前
        // 确保在 Spring Security 标准认证流程之前，先尝试解析和校验 JWT Token
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置密码编码器
     * <p>
     * 使用 Argon2 算法，它是目前最安全的密码哈希算法之一，抗 GPU/ASIC 攻击能力强。
     * 参数配置：
     * - saltLength: 16 (盐长度)
     * - hashLength: 32 (哈希长度)
     * - parallelism: 1 (并行度，线程数)
     * - memory: 16384 (内存消耗，KB)
     * - iterations: 2 (迭代次数)
     * </p>
     *
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 16384, 2);
    }

    /**
     * 暴露 AuthenticationManager Bean
     * <p>
     * 在 Controller 中（如登录接口）需要手动调用 authenticate() 方法进行认证，
     * 所以需要将 AuthenticationManager 暴露为 Bean 注入使用。
     * </p>
     *
     * @param config AuthenticationConfiguration 配置对象
     * @return AuthenticationManager 认证管理器实例
     * @throws Exception 获取异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
