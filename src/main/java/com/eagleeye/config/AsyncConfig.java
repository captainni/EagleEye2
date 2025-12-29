package com.eagleeye.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务线程池配置
 *
 * @author eagleeye
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 政策分析专用线程池
     */
    @Bean("policyAnalysisExecutor")
    public Executor policyAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("policy-analysis-");
        executor.initialize();
        return executor;
    }

    /**
     * 竞品分析专用线程池
     */
    @Bean("competitorAnalysisExecutor")
    public Executor competitorAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("competitor-analysis-");
        executor.initialize();
        return executor;
    }
}
