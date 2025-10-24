package org.example.chatreact.evaluator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
public class LoadDataToRedis {

    public void loadDataToRedis(@Autowired VectorStore vectorStore) throws IOException {
        log.info("vectorStore={}", vectorStore.getClass());
        List<QaJson> list = loadData();
        for (QaJson qaJson : list) {
            log.info("qaJson->docs:{}", qaJson.docs());
            for (String doc : qaJson.docs()) {
                Document phoneDoc = Document.builder()
                        .text(doc) // 文本内容
                        .build();
                vectorStore.add(List.of(phoneDoc));
            }
        }
    }

    private List<QaJson> loadData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("rag_evaluator/rag.json");
        return mapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }
}
