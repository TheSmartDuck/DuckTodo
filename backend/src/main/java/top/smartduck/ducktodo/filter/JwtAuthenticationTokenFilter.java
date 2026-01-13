package top.smartduck.ducktodo.filter;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.smartduck.ducktodo.model.dto.LoginUserDto;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取token
        String token = resolveToken(request);

        if (StrUtil.isNotBlank(token)) {
            // 2. 校验token
            if (jwtUtil.isValid(token)) {
                try {
                    // 3. 解析token获取用户信息
                    User user = jwtUtil.parseToken(token);
                    
                    // 4. 构建 Authentication 对象
                    // 由于是无状态JWT，这里不强制查库，直接信任Token中的基本信息构建Principal
                    // 密码设为null，权限暂为空
                    LoginUserDto loginUser = new LoginUserDto(user, null);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
                    
                    // 5. 存入 SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    // 6. 设置 request 属性以兼容旧代码 (CommonUtil.getCurrentUser)
                    request.setAttribute("currentUser", user);

                } catch (Exception e) {
                    // 解析失败，忽略，走后续流程（可能导致 403）
                    log.error("JWT authentication failed: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return request.getHeader("token");
    }
}
