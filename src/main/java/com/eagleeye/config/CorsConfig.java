package com.eagleeye.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 允许所有路径进行跨域请求
                registry.addMapping("/**")
                        // 允许来自前端开发服务器的请求 (支持多个端口)
                        .allowedOrigins("http://localhost:8088", "http://localhost:8089")
                        // 允许所有请求方法 (GET, POST, PUT, DELETE, etc.)
                        .allowedMethods("*")
                        // 允许所有请求头
                        .allowedHeaders("*")
                        // 是否允许发送Cookie等凭证信息 (根据后续认证需求可能需要设置为true)
                        .allowCredentials(false)
                        // 预检请求的有效期，单位为秒
                        .maxAge(3600);
            }
        };
    }
} 