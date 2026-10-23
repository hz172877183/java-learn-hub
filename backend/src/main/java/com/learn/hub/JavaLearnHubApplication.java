package com.learn.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口：启动 Spring Boot，并在启动后触发内容种子导入。
 */
@SpringBootApplication
public class JavaLearnHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaLearnHubApplication.class, args);
    }
}
