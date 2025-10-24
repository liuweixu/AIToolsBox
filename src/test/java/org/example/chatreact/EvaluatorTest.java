package org.example.chatreact;

import com.alibaba.cloud.ai.evaluation.AnswerCorrectnessEvaluator;
import com.alibaba.cloud.ai.evaluation.AnswerFaithfulnessEvaluator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.chatreact.evaluator.LoadDataToRedis;
import org.example.chatreact.evaluator.QaJson;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

@SpringBootTest
@Slf4j
public class EvaluatorTest {

    @Test
    void testEvaluator(
            @Autowired ChatClient.Builder chatClientBuilder,
            @Autowired VectorStore vectorStore) throws IOException {

        LoadDataToRedis loadDataToRedis = new LoadDataToRedis();
        loadDataToRedis.loadDataToRedis(vectorStore);
        // 从向量数据库召回
        QuestionAnswerAdvisor questionAdvisor = QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.2) //相似度阈值，只有大于等于该值才会被返回（取值范围:0-1），默认是0（没有相似度排除）
                        .topK(5) //返回相似度排名前2的文档，默认是4
                        .build())
                .build();

        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(questionAdvisor) // 添加向量数据库advisor
                .build();

        AnswerCorrectnessEvaluator answerCorrectnessEvaluator
                = new AnswerCorrectnessEvaluator(chatClientBuilder);
        AnswerFaithfulnessEvaluator faithfulnessEvaluator
                = new AnswerFaithfulnessEvaluator(chatClientBuilder, new ObjectMapper());

        // 1. 读取JSON
        List<QaJson> list = loadData();
        // 2. 逐条评估
        list.forEach(qa -> {
            List<Document> docs = qa.docs().stream().map(Document::new).toList();

            // 向AI 获取答案
            String aiAnswer = chatClient.prompt()
                    .user(qa.question())
                    .call()
                    .content();

            EvaluationRequest request = new EvaluationRequest(qa.question(), docs, aiAnswer);

            EvaluationResponse correct  = answerCorrectnessEvaluator.evaluate(request);
            EvaluationResponse faithful = faithfulnessEvaluator.evaluate(request);

            System.out.printf("Q: %s%nA: %s%n", qa.question(), aiAnswer);
            System.out.printf("准确率分数 = %.2f , 事实性分数= %.2f%n%n",
                    correct.getScore(), faithful.getScore());
        });
    }

    private List<QaJson> loadData() throws IOException {
        ObjectMapper      mapper   = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("rag_evaluator/rag.json");
        return mapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }
}
