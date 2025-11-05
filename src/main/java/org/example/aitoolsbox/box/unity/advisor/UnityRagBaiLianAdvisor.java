package org.example.aitoolsbox.box.unity.advisor;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeConnectionProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.common.RRFFusionProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class UnityRagBaiLianAdvisor {

    private static final String indexName = "Unity开发学习";

    // 自定义prompt模板，允许模型在没有知识库内容时基于自身知识回答
    PromptTemplate promptTemplate = new PromptTemplate("""
            以下为相关背景信息（如果为空，则基于你的知识回答）：
            ---------------------
            {context}
            ---------------------
            
            根据提供的背景信息回答问题。
            如果背景信息为空或与问题无关，你可以基于你的知识来回答。
            查询：{query}
            回答：
            """);

    public RetrievalAugmentationAdvisor advisor(ChatModel chatModel, DashScopeConnectionProperties properties) {

        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);
        // 1. Pre-Retrieval模块
        // 1.1 QueryTransformer：将问题转换为中文
        QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .targetLanguage("Chinese")
                .build();
        // 1.2 MultiQueryExpander：将问题变成多个角度的问题
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder) // 需要引入大模型
                .numberOfQueries(3) // 生成新的问题条数
                .includeOriginal(true) // 是否包括原先问题，默认是true
                .build();
        DocumentRetriever retriever = new DashScopeDocumentRetriever(
                DashScopeApi.builder().apiKey(properties.getApiKey()).build(),
                DashScopeDocumentRetrieverOptions.builder().withIndexName(indexName).build());

        // 自定义DocumentJoiner：将多个查询结果的文档采用RRF算法进行去重和排序，
        DocumentJoiner documentJoiner = new DocumentJoiner() {
            private final RRFFusionProcessor fusionProcessor = new RRFFusionProcessor(60);

            @Override
            public List<Document> join(Map<Query, List<List<Document>>> documentsForQuery) {
                if (documentsForQuery != null) {
                    List<List<Document>> documents = new ArrayList<>();
                    Iterator<Query> iterator = documentsForQuery.keySet().iterator();
                    while (iterator.hasNext()) {
                        List<List<Document>> lists = documentsForQuery.get(iterator.next());
                        if (lists != null) {
                            documents.add(lists.get(0));
                        }
                    }
                    return fusionProcessor.fuseRankings(documents);
                } else {
                    return List.of();
                }
            }
        };

        return RetrievalAugmentationAdvisor
                .builder()
                .queryTransformers(queryTransformer)
                .queryExpander(queryExpander)
                .documentRetriever(retriever)
                .documentJoiner(documentJoiner)
                .queryAugmenter(ContextualQueryAugmenter
                        .builder()
                        .promptTemplate(promptTemplate)
                        // .allowEmptyContext(true) // 这个效果不好
                        .build())
                .build();
    }

}
