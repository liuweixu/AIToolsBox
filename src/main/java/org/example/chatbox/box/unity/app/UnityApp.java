package org.example.chatbox.box.unity.app;

import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.chatbox.box.unity.advisor.MyLoggerAdvisor;
import org.example.chatbox.box.unity.chat_history.service.ChatHistoryService;
import org.example.chatbox.models.ChatModelFactory;
import org.example.chatbox.enums.ChatModelEnum;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class UnityApp {

    private final ChatModelFactory chatModelFactory;

    // 缓存每个平台的 ChatClient
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    @Resource
    private RedissonRedisChatMemoryRepository redisChatMemoryRepository;

    // 导入工厂
    @Resource
    private ToolCallback[] toolCallbacks;

    @Resource
    private ChatHistoryService chatHistoryService;

    // 百炼知识库RAG
    @Resource
    private Advisor unityRagBaiLianAdvisor;

    /**
     * 系统提示词
     */
    private static final String SYSTEM_PROMPT = """
            你是一位经验丰富的 Unity 游戏开发专家兼教学导师，精通 C# 编程、游戏架构设计、组件系统、动画、物理与 UI 开发。
            你的目标是帮助用户系统地学习 Unity 与 C#，并解决他们在实际开发中遇到的问题。
            
            在对话中，你应当：
            
            使用自然、鼓励性的教学语气。
            
            主动提出引导性问题，了解用户的项目目标、学习阶段和困惑。
            
            先讲整体思路，再提供清晰的实现步骤与 C# 代码示例。
            
            在讲解时结合 Unity 实际内容（如 GameObject、Component、Script、Inspector、Prefab、Scene 等）。
            
            对复杂问题分步骤拆解，必要时通过提问确认用户意图。
            
            让用户不仅获得解决方案，也能理解背后的逻辑与原理，逐步建立完整的 Unity 与 C# 开发思维。
            
            始终以一位亲切、专业的 Unity 导师身份进行交流，帮助用户真正掌握 Unity 与 C# 游戏开发技能。
            """;
    @Autowired
    private ChatMemory chatMemory;


    /**
     * 构造对象
     * 现在改为引入ChatModelFactoryu
     *
     * @param chatModelFactory
     */
    public UnityApp(ChatModelFactory chatModelFactory) {
        this.chatModelFactory = chatModelFactory;
    }

    /**
     * 会话记忆引入Redis
     *
     * @param chatModelName
     * @return
     */
    private ChatClient createChatClient(String chatModelName, String unityId) {
        ChatModel chatModel = this.chatModelFactory.getChatModel(chatModelName);
        log.info("ChatModel for AIPlatform: {}", chatModel);
        // 基于Redis的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(20)
                .build();

        chatHistoryService.loadChatHistoryToMemory(Long.valueOf(unityId), chatMemory, 20);
        log.info("加载对话历史");
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .conversationId(unityId)
                                .build(),
                        // 自定义日志Advisor
                        new MyLoggerAdvisor()
                )
                .build();
    }


    /**
     * 基础对话方法 可以实现多轮对话
     *
     * @param message
     * @param unityId
     * @param modelName
     * @return
     */
    public String doChat(String message, String unityId, String modelName) {
        ChatClient chatClient = createChatClient(modelName, unityId);
        log.info("选择的模型：{}", modelName);
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .call()
                .chatResponse();
        String content = null;
        if (response != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("Chat response: {}", content);
        return content;
    }

    /**
     * Unity开发学习报告类
     *
     * @param title
     * @param suggestions
     */
    public record UnityReport(String title, List<String> suggestions) {
    }

    public UnityReport doChatWithReport(String message, String unityId, String modelName) {
        ChatClient chatClient = createChatClient(modelName, unityId);
        UnityReport unityReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话要生成学习报告结果，标题为{用户名}的学习报告，内容为建议列表")
                .user(message)
                .call()
                .entity(UnityReport.class);
        log.info("Unity report: {}", unityReport);
        return unityReport;
    }


    /**
     * RAG调用，此处使用百炼智能体
     *
     * @param message
     * @param unityId
     * @param modelName
     * @return
     */
    public String doChatWithRag(String message, String unityId, String modelName) {
        ChatClient chatClient = createChatClient(modelName, unityId);
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(new MyLoggerAdvisor())
                .advisors(unityRagBaiLianAdvisor)
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }

    /**
     * UnityApp工具调用
     *
     * @param message
     * @param unityId
     * @param modelName
     * @return
     */
    public String doChatWithTools(String message, String unityId, String modelName) {
        ChatClient chatClient = createChatClient(modelName, unityId);
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                //TODO  开启日志，观察效果
//                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbacks)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * Flux 流式输出
     *
     * @param message
     * @param unityId
     * @param modelName
     * @return
     */
    public Flux<String> doChatByStream(String message, String unityId, String modelName) {
        log.info("unityid: {}", unityId);
        ChatClient chatClient = createChatClient(modelName, unityId);
        return chatClient
                .prompt()
                .user(message)
                .advisors(unityRagBaiLianAdvisor) // 引入百炼知识库rag
                .toolCallbacks(toolCallbacks)
                .stream()
                .content();
    }

    /**
     * 对用户输入的语句生成10字以内的摘要
     *
     * @param message 用户输入内容
     * @return
     */


    public String summaryResponse(String message) {

        if (message.length() <= 10) {
            return message;
        } else {
            String modelName = ChatModelEnum.DASHSCOPE.name();
            ChatModel chatModel = this.chatModelFactory.getChatModel(modelName);
            String summaryPrompt = """
                    不需要任何回答，只需要对`{message}`总结一句话。
                    需要有一定特性介绍，比如XX介绍、讲解XX、XX的总结、实现XX的方案等，控制10字以内。
                    如果{message}长度低于10个字，无需总结，直接返回{message}即可
                    """;
            Map<String, Object> map = new HashMap<>();
            map.put("message", message);
            PromptTemplate promptTemplate = new PromptTemplate(summaryPrompt);
            String prompt = promptTemplate.render(map);
            return chatModel.call(prompt);
        }
    }
}

