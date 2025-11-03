package org.example.aitoolsbox.config;

import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

//@Configuration
public class RedisMemorySAAConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;
    @Value("${spring.data.redis.timeout}")
    private int redisTimeout;

    @Bean
    public RedissonRedisChatMemoryRepository chatMemoryRepository() {

        return RedissonRedisChatMemoryRepository
                .builder()
                .host(redisHost)
                .port(redisPort)
                .timeout(redisTimeout)
                .build();
    }

}
