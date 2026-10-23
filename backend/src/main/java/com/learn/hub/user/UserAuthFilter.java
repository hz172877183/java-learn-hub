package com.learn.hub.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 普通用户接口的 JWT 校验，只保护 /api/v1/me/**。
 */
@Component
public class UserAuthFilter extends OncePerRequestFilter {

    public static final String ATTR_USER_ID = "currentUserId";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public UserAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * 只保护 /api/v1/me/**；OPTIONS 预检直接放行，避免浏览器拿不到真实请求的响应头。
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean protectedPath = path.startsWith("/api/v1/me/") || path.equals("/api/v1/me");
        boolean preflight = "OPTIONS".equalsIgnoreCase(request.getMethod());

        if (protectedPath && !preflight) {
            String header = request.getHeader("Authorization");
            String token = header != null && header.startsWith(PREFIX)
                    ? header.substring(PREFIX.length()).trim()
                    : null;
            var userId = token == null ? java.util.Optional.<Long>empty() : jwtService.parseUserId(token);
            if (userId.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"code\":\"UNAUTHORIZED\",\"message\":\"请先登录，或登录已失效\"}");
                return;
            }
            request.setAttribute(ATTR_USER_ID, userId.get());
        }
        filterChain.doFilter(request, response);
    }
}
