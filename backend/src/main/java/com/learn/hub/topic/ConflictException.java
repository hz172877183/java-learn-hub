package com.learn.hub.topic;
/**
 * 资源冲突（如 slug、用户名重复），统一映射为 409。
 */

public class ConflictException extends RuntimeException {

    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
