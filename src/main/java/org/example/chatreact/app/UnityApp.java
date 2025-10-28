package org.example.chatreact.app;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.extern.slf4j.Slf4j;
import org.example.chatreact.advisor.MyLoggerAdvisor;
import org.example.chatreact.chatmemory.FileBasedChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UnityApp {

    private final ChatClient chatClient;

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
     *
     * @param dashscopeChatModel
     */
    public UnityApp(ChatModel dashscopeChatModel) {
        // 基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志Advisor
                        new MyLoggerAdvisor()
                )
                .build();
        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
    }

    /**
     * 基础对话方法 可以实现多轮对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
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

    public UnityReport doChatWithReport(String message, String chatId) {
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
}

