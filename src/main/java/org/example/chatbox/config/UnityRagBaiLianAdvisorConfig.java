package org.example.chatbox.config;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeConnectionProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@EnableConfigurationProperties({DashScopeConnectionProperties.class})
public class UnityRagBaiLianAdvisorConfig {

    private static final String indexName = "Unity开发学习";

    @Bean
    public Advisor unityRagBaiLianAdvisor(DashScopeConnectionProperties properties) {
        DocumentRetriever retriever = new DashScopeDocumentRetriever(
                DashScopeApi.builder().apiKey(properties.getApiKey()).build(),
                DashScopeDocumentRetrieverOptions.builder().withIndexName(indexName).build()
        );
        return RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(retriever)
                .build();
    }
}
