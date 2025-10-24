package org.example.chatreact.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 查询生成服务
 * 实现类似Python LangChain中的多角度问题扩展功能
 */
@Service
@Slf4j
public class QueryGeneratorService {
    
    private final ChatClient chatClient;
    
    public QueryGeneratorService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    /**
     * 生成多个角度的查询变体
     * 类似于Python中的generate_queries功能
     * @param originalQuestion 原始问题
     * @return 查询变体列表
     */
    public List<String> generateQueryVariants(String originalQuestion) {
        log.info("开始为问题生成多个角度的查询变体: {}", originalQuestion);
        
        try {
            // 使用类似Python中的prompt模板
            PromptTemplate questionPrompt = new PromptTemplate("""
                你是一个人工智能语言模型助手。你的任务是生成给定用户的3个不同版本从矢量数据库中检索相关文档的问题。
                通过对用户问题产生多种视角，你的目标是帮助用户克服一些限制基于距离的相似度搜索。提供这些选择用换行符分隔的问题。
                原始问题: {question}
                """);
            
            String response = chatClient.prompt()
                    .user(questionPrompt.create(Map.of("question", originalQuestion)).toString())
                    .call()
                    .content();
            
            // 解析响应，按换行符分割
            List<String> variants = Arrays.stream(response.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            
            // 确保包含原始问题
            List<String> result = new ArrayList<>();
            result.add(originalQuestion);
            result.addAll(variants);
            
            log.info("生成了 {} 个查询变体: {}", result.size(), result);
            return result;
            
        } catch (Exception e) {
            log.error("生成查询变体时出错: {}", e.getMessage(), e);
            // 如果出错，返回简单的变体
            return generateSimpleVariants(originalQuestion);
        }
    }
    
    /**
     * 生成简单的查询变体（备用方案）
     * @param originalQuestion 原始问题
     * @return 简单变体列表
     */
    public List<String> generateSimpleVariants(String originalQuestion) {
        List<String> variants = new ArrayList<>();
        variants.add(originalQuestion);
        
        // 生成一些简单的变体
        if (originalQuestion.contains("什么")) {
            variants.add(originalQuestion.replace("什么", "如何"));
        }
        if (originalQuestion.contains("如何")) {
            variants.add(originalQuestion.replace("如何", "什么"));
        }
        if (originalQuestion.contains("是")) {
            variants.add(originalQuestion.replace("是", "包括"));
        }
        
        // 添加通用变体
        variants.add("请详细解释：" + originalQuestion);
        variants.add("关于" + originalQuestion + "的更多信息");
        
        log.info("生成了 {} 个简单查询变体: {}", variants.size(), variants);
        return variants;
    }
}
