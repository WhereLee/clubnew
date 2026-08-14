package com.club.config;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * 测试配置：替代异步执行器为同步，mock RedissonClient
 */
@Configuration
@Profile("test")
public class TestConfig {

    /**
     * 测试环境下使用同步执行器，确保异步操作立即完成
     */
    @Bean("clubAsyncExecutor")
    @Primary
    public TaskExecutor testClubAsyncExecutor() {
        return new SyncTaskExecutor();
    }

    /**
     * 测试环境下 mock RedissonClient，避免连接真实 Redis
     */
    @Bean
    @Primary
    public RedissonClient redissonClient() {
        return Mockito.mock(RedissonClient.class);
    }
}
