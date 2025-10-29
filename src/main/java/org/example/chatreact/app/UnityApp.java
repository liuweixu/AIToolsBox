package org.example.chatreact.app;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.chatreact.advisor.MyLoggerAdvisor;
import org.example.chatreact.chatmodel.ChatModelFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

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
    private ChatMemoryRepository chatMemoryRepository;

    // 导入工厂
    @Resource
    private ToolCallback[] toolCallbacks;

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
    private ChatClient createChatClient(String chatModelName) {
        ChatModel chatModel = this.chatModelFactory.getChatModel(chatModelName);
        log.info("ChatModel for AIPlatform: {}", chatModel);
        // 基于Redis的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志Advisor
                        new MyLoggerAdvisor()
                )
                .build();
    }


    @PostConstruct
    public void initAllChatClients() {
        for (String modelName : List.of("DASHSCOPE", "DEEPSEEK", "OLLAMA", "ZHIPUAI")) {
            chatClientCache.computeIfAbsent(modelName, this::createChatClient);
        }
        log.info("所有 ChatClient 初始化完成: {}", chatClientCache.keySet());
    }


    /**
     * 基础对话方法 可以实现多轮对话
     *
     * @param message
     * @param chatId
     * @param modelName
     * @return
     */
    public String doChat(String message, String chatId, String modelName) {
        ChatClient chatClient = chatClientCache.get(modelName);
        log.info("选择的模型：{}", modelName);
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param(ChatMemory.CONVERSATION_ID, 10))
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

    public UnityReport doChatWithReport(String message, String chatId, String modelName) {
        ChatClient chatClient = chatClientCache.get(modelName);
        UnityReport unityReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话要生成学习报告结果，标题为{用户名}的学习报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param(ChatMemory.CONVERSATION_ID, 10))
                .call()
                .entity(UnityReport.class);
        log.info("Unity report: {}", unityReport);
        return unityReport;
    }

    public String doChatWithTools(String message, String chatId, String modelName) {
        ChatClient chatClient = chatClientCache.get(modelName);
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param(ChatMemory.CONVERSATION_ID, 10))
                //TODO  开启日志，观察效果
//                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbacks)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}

