package org.example.chatreact.query;

import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.Query;
import org.springframework.stereotype.Component;
import org.example.chatreact.service.QueryGeneratorService;

import java.util.List;

/**
 * 自定义查询扩展器
 * 实现类似Python LangChain中的查询扩展功能
 */
@Component
public class CustomQueryExpander implements QueryExpander {
    
    private final QueryGeneratorService queryGeneratorService;
    
    public CustomQueryExpander(QueryGeneratorService queryGeneratorService) {
        this.queryGeneratorService = queryGeneratorService;
    }
    
    @Override
    public List<Query> expand(Query query) {
        // 生成查询变体
        List<String> variants = queryGeneratorService.generateQueryVariants(query.toString());
        
        // 转换为Query对象列表
        return variants.stream()
                .map(Query::new)
                .toList();
    }
}
