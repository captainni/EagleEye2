package com.eagleeye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 金融资讯智能跟踪平台主应用类
 */
@SpringBootApplication
@MapperScan("com.eagleeye.repository")
@EnableAsync
public class EagleEyeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EagleEyeApplication.class, args);
    }

} 