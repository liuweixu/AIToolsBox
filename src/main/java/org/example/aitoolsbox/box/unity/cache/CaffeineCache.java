package org.example.aitoolsbox.box.unity.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.box.unity.chat_history.entity.ChatClientKey;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class CaffeineCache {

    @Bean
    public Cache<ChatClientKey, ChatClient> serviceCache() {
        return Caffeine
                .newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .expireAfterWrite(Duration.ofMinutes(30))
                .removalListener((key, value, cause) -> {
                    log.info("AI 服务实例被移除：appId: {}, 原因: {}", key, cause);
                })
                .build();
    }
}
