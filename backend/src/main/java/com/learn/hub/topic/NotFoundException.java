package com.learn.hub.topic;
/**
 * 资源不存在，统一映射为 404，并携带业务码。
 */

public class NotFoundException extends RuntimeException {

    private final String code;

    public NotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
