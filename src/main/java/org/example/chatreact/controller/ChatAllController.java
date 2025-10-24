package org.example.chatreact.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.chatreact.algorithms.RRFFusionProcessor;
import org.example.chatreact.query.CustomQueryExpander;
import org.example.chatreact.service.QueryGeneratorService;
import org.example.chatreact.service.RRFService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Slf4j
public class ChatAllController {
    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;

    public ChatAllController(ChatClient.Builder chatClientBuilder,
                             VectorStore vectorStore, ChatMemory chatMemory,
                             QueryGeneratorService queryGeneratorService,
                             SyncMcpToolCallbackProvider syncMcpToolCallbackProvider) {
        // 1. Pre-Retrieval模块
        // 使用自定义的查询扩展器，避免Spring AI的循环问题
        CustomQueryExpander customQueryExpander = new CustomQueryExpander(queryGeneratorService);

        // 2. Retrieval模块
        // 2.1 查询向量数据库
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.3) // 相似度大于等于0.3
                .topK(2)
                .build();
        // 2.2 自定义DocumentJoiner：将多个查询结果的文档采用RRF算法进行去重和排序，
        DocumentJoiner documentJoiner = new DocumentJoiner() {
            private final RRFFusionProcessor fusionProcessor = new RRFFusionProcessor(60);
            @Override
            public List<Document> join(Map<Query, List<List<Document>>> documentsForQuery) {
                if (documentsForQuery != null) {
                    // documentsForQuery是要重写的，故而先把里面的值元素取出来。
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


        // 3. Generation模块
        // 3.1 QueryAugmenter：组装最后的问题
        PromptTemplate promptTemplate = new PromptTemplate("""
                以下为相关背景信息。
                ---------------------
                {context}
                ---------------------
                
                根据提供的背景信息且没有先入为主的观念，回答问题。
                请遵循以下规则：
                1. 如果答案不在所提供的信息中，那就直接说你不知道。
                2. 避免使用诸如“根据上下文……”或“所提供的信息……”这样的表述。
                查询：{query}
                回答：
                """);
        QueryAugmenter queryAugmenter = ContextualQueryAugmenter
                .builder()
                .promptTemplate(promptTemplate)
                .build();

        // 通过Advisor方式，对向量数据库进行封装
        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                // 使用自定义查询扩展器，支持RRF算法
                .queryExpander(customQueryExpander)
                .documentRetriever(documentRetriever)
                .documentJoiner(documentJoiner)
                .queryAugmenter(queryAugmenter)
                .build();

        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(ragAdvisor, memoryAdvisor)
                .build();
        this.syncMcpToolCallbackProvider = syncMcpToolCallbackProvider;
    }

    /**
     * 都导入rag和搜狗mcp
     * @param memoryId
     * @param message
     * @return
     */
    @GetMapping(value = "/chat")
    public Flux<ServerSentEvent<String>> chat(
            @RequestParam int memoryId,
            @RequestParam String message) {
        log.info("[ai/chat] memoryId:{} message:{}", memoryId, message);
        return this.chatClient.prompt()
                .toolCallbacks(this.syncMcpToolCallbackProvider)
                .user(message)
                // 维护多轮对话
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, memoryId))
                .stream()
                .content()
                .map( chuck -> ServerSentEvent.<String>builder()
                        .data(chuck)
                        .build());
    }

}
