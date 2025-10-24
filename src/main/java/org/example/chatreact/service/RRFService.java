package org.example.chatreact.service;

import lombok.extern.slf4j.Slf4j;
import org.example.chatreact.algorithms.RRFFusionProcessor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * RRF (Reciprocal Rank Fusion) 服务
 * 实现类似Python LangChain中的RRF算法
 */
@Service
@Slf4j
public class RRFService {
    
    private final VectorStore vectorStore;
    private final RRFFusionProcessor rrfProcessor;
    
    public RRFService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.rrfProcessor = new RRFFusionProcessor(60); // k=60，与Python版本一致
    }
    
    /**
     * 执行RRF算法，检索并融合文档
     * @param queries 查询列表
     * @param topK 每个查询返回的文档数量
     * @return 融合后的文档列表
     */
    public List<Document> performRRF(List<String> queries, int topK) {
        log.info("开始执行RRF算法，查询数量: {}, topK: {}", queries.size(), topK);
        
        try {
            // 为每个查询检索文档
            List<List<Document>> allResults = new ArrayList<>();
            
            for (String query : queries) {
                log.info("正在检索查询: {}", query);
                List<Document> docs = vectorStore.similaritySearch(query);
                // 限制返回的文档数量
                if (docs.size() > topK) {
                    docs = docs.subList(0, topK);
                }
                log.info("查询 '{}' 检索到 {} 个文档", query, docs.size());
                allResults.add(docs);
            }
            
            // 使用RRF算法融合结果
            List<Document> fusedResults = rrfProcessor.fuseRankings(allResults);
            log.info("RRF融合后返回 {} 个文档", fusedResults.size());
            
            return fusedResults;
            
        } catch (Exception e) {
            log.error("RRF算法执行出错: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 从文档列表中构建上下文
     * @param documents 文档列表
     * @return 构建的上下文字符串
     */
    public String getContextFromDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        for (Document doc : documents) {
            context.append(doc.getText()).append("\n");
        }
        
        log.info("构建的上下文长度: {}", context.length());
        return context.toString();
    }
    
    /**
     * 执行完整的RRF流程
     * @param originalQuery 原始查询
     * @param queryVariants 查询变体
     * @param topK 每个查询返回的文档数量
     * @return 融合后的文档列表
     */
    public List<Document> executeFullRRF(String originalQuery, List<String> queryVariants, int topK) {
        log.info("执行完整RRF流程，原始查询: {}", originalQuery);
        
        // 合并原始查询和变体
        List<String> allQueries = new ArrayList<>();
        allQueries.add(originalQuery);
        allQueries.addAll(queryVariants);
        
        return performRRF(allQueries, topK);
    }
}
