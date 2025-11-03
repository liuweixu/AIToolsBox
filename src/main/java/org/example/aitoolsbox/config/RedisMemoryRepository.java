package org.example.aitoolsbox.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedisMemoryRepository implements ChatMemoryRepository {

    private static final String REDIS_KEY_PREFIX = "chatmemory-redis:";

    private final RedisTemplate<String, Message> redisTemplate;

    @Value("${chat.memory.redis.ttl-minutes}")
    private long ttlMinutes;

    public RedisMemoryRepository(RedisTemplate<String, Message> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @NotNull
    @Override
    public List<String> findConversationIds() {
        Set<String> keys = this.redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        return keys.stream().toList();
    }

    @NotNull
    @Override
    public List<Message> findByConversationId(@NotNull String conversationId) {
        return Objects.requireNonNull(this.redisTemplate.opsForList().range(REDIS_KEY_PREFIX + conversationId, 0, -1));
    }

    @Override
    public void saveAll(@NotNull String conversationId, @NotNull List<Message> messages) {
        String key = REDIS_KEY_PREFIX + conversationId;

        // 由于每次的messages都会获取到之前的数据，因此要先删除，在插入，防止重复
        this.redisTemplate.delete(key);
        // 插入新消息
        this.redisTemplate.opsForList().rightPushAll(key, messages);
        // 设置超时时间（设置一小时）
        this.redisTemplate.expire(key, ttlMinutes, TimeUnit.MINUTES);
    }

    @Override
    public void deleteByConversationId(@NotNull String conversationId) {
        this.redisTemplate.delete(REDIS_KEY_PREFIX + conversationId);
    }
}
