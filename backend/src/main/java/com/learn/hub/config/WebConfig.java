package com.learn.hub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web 配置：按来源放行跨域请求，并覆盖管理端需要的写方法。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final List<String> origins;

    public WebConfig(@Value("${learn-hub.cors-origins}") List<String> origins) {
        this.origins = origins;
    }

    /**
     * 按来源放行跨域；方法必须覆盖管理端实际使用的写操作，否则预检会直接失败。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
