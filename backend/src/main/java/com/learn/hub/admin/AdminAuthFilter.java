package com.learn.hub.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 管理端写接口的固定 Bearer Token 校验。
 * 只保护 /api/v1/admin/**，读接口保持公开。
 */
@Component
public class AdminAuthFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final byte[] expectedToken;

    public AdminAuthFilter(@Value("${learn-hub.admin-token}") String adminToken) {
        this.expectedToken = adminToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean protectedPath = path.startsWith("/api/v1/admin/");
        boolean preflight = "OPTIONS".equalsIgnoreCase(request.getMethod());

        if (protectedPath && !preflight && !authorized(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":\"UNAUTHORIZED\",\"message\":\"缺少或错误的管理令牌\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean authorized(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(PREFIX)) {
            return false;
        }
        byte[] provided = header.substring(PREFIX.length()).getBytes(StandardCharsets.UTF_8);
        // 常量时间比较，避免通过响应时间猜测令牌
        return MessageDigest.isEqual(expectedToken, provided);
    }
}
