package com.learn.hub.api;

import com.learn.hub.topic.ConflictException;
import com.learn.hub.topic.NotFoundException;
import com.learn.hub.user.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Optional;
import java.util.UUID;

/**
 * 全局异常处理：把业务异常与框架异常映射为稳定的状态码和统一错误体。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ApiError(String code, String message, String traceId) {
    }

    /**
     * 业务上的“不存在”，返回 404 与业务码（如 TOPIC_NOT_FOUND）。
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(ex.getCode(), ex.getMessage(), traceId()));
    }

    /**
     * 参数或请求体不合法，返回 400。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("BAD_REQUEST", ex.getMessage(), traceId()));
    }

    /**
     * 唯一键冲突，返回 409。
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(ex.getCode(), ex.getMessage(), traceId()));
    }

    /**
     * 未认证或凭证错误，返回 401。
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> unauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(ex.getCode(), ex.getMessage(), traceId()));
    }

    /**
     * 未知路径：Spring 6 会抛 NoResourceFoundException，这里统一成 404，避免落进 500 兜底。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> noResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", "接口不存在: /" + ex.getResourcePath(), traceId()));
    }

    /**
     * 路径存在但不支持该方法，返回 405。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiError("METHOD_NOT_ALLOWED", "不支持的请求方法: " + ex.getMethod(), traceId()));
    }

    /**
     * Content-Type 不支持，返回 415。
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> unsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ApiError("UNSUPPORTED_MEDIA_TYPE", "请求体需要 application/json", traceId()));
    }

    /**
     * 请求体不是合法 JSON，返回 400 而不是 500。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadableBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("BAD_REQUEST", "请求体 JSON 无法解析", traceId()));
    }

    /**
     * 兜底：记录完整堆栈，对外只返回通用文案与 traceId，避免泄露内部细节。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> fallback(Exception ex, HttpServletRequest request) {
        log.error("未处理异常: {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_ERROR", "服务暂时不可用", traceId()));
    }

    private String traceId() {
        return Optional.ofNullable(MDC.get("traceId")).orElseGet(() -> UUID.randomUUID().toString());
    }
}
