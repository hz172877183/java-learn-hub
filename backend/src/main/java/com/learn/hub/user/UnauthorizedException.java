package com.learn.hub.user;
/**
 * 未认证或凭证错误，统一映射为 401。
 */

public class UnauthorizedException extends RuntimeException {

    private final String code;

    public UnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
