package org.example.aitoolsbox.config;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
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
    public VectorStore vectorStore(DashScopeEmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPool(), embeddingModel)
                .indexName("spring-ai-index")
                .initializeSchema(true)
                .prefix("embedding")
                .build();
    }

    public JedisPooled jedisPool() {
        return new JedisPooled("redis://localhost:6379/0");
    }
}
