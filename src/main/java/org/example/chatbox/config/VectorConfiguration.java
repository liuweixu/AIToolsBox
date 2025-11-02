package org.example.chatbox.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
class VectorConfiguration {
    /**
     * Redis-stack可以作为向量数据库
     * 注册Redis-stack为VectorStore
     * 不过如果需要读取一些文件，就要写另外的DocumentReader等。
     * 此处使用本地OLLMA的向量嵌入模型
     *
     * @param embeddingModel
     * @return
     */
    @Bean
    public VectorStore vectorStore(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPool(), embeddingModel)
                .indexName("spring-ai-index")
                .initializeSchema(true)
                .prefix("spring-ai-redis-prefix")
                .build();
    }

    public JedisPooled jedisPool() {
        return new JedisPooled("redis://localhost:6379/0");
    }
}
