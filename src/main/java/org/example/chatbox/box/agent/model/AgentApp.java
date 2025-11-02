package org.example.chatbox.box.agent.model;


import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.chatbox.box.agent.service.ChatAgentHistoryService;
import org.example.chatbox.box.unity.advisor.MyLoggerAdvisor;
import org.example.chatbox.models.ChatModelFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class AgentApp {

    @Resource
    private AIManus aiManus;

    private final ChatModelFactory chatModelFactory;

    @Resource
    private ChatAgentHistoryService chatAgentHistoryService;

    /**
     * Redis会话记忆
     */
    @Resource
    private RedissonRedisChatMemoryRepository redisChatMemoryRepository;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;


    public AgentApp(ChatModelFactory chatModelFactory) {
        this.chatModelFactory = chatModelFactory;
    }

    /**
     * 创建通用智能体
     *
     * @param chatModelName
     * @return
     */
    private ChatClient createChatClient(String chatModelName, Long agentId) {
        ChatModel chatModel = this.chatModelFactory.getChatModel(chatModelName);
        log.info("ChatModel for AIPlatform: {}", chatModel);
//        // 基于Redis的对话记忆
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(redisChatMemoryRepository)
//                .maxMessages(21)
//                .build();

//        chatAgentHistoryService.loadAgentHistoryToMemory(agentId, chatMemory, 21);
        log.info("加载对话历史");
        return ChatClient.builder(chatModel)
//                .defaultAdvisors(
//                        MessageChatMemoryAdvisor
//                                .builder(chatMemory)
//                                .conversationId(String.valueOf(agentId))
//                                .build(),
//                        // 自定义日志Advisor
//                        new MyLoggerAdvisor()
//                )
                .build();
    }

    /**
     * SseEmitter流式输出
     *
     * @param message
     * @param modelName
     * @param agentId
     * @return
     */
    public SseEmitter doChatWithManus(String message, String modelName, Long agentId) {
        ChatClient chatClient = createChatClient(modelName, agentId);
        aiManus.setChatClient(chatClient);
        return aiManus.runStream(message, agentId);
    }
}
