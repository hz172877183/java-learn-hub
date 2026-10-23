package com.learn.hub.user;
/**
 * 认证接口 DTO。
 */

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(String username, String password, String displayName) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record UserView(Long id, String username, String displayName) {
    }

    public record AuthResponse(String token, UserView user) {
    }
}
