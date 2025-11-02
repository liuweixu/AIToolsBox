package org.example.chatbox.box.agent.model;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.example.chatbox.enums.AgentState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存了工具调用信息的响应信息
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false) // 注意！！！！
                .build();
    }

    /**
     * 处理当前状态并执行下一步行动
     * 这部分需要关注的就是看是否调用了工具消息
     *
     * @return 是否需要行动，true表示需要执行，false表示不需要执行
     */
    @Override
    public boolean think() {
        // TODO 先创建消息列表
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        // TODO 先用Prompt类创建prompt，里面输入的参数一般是系统信息、用户信息等。
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 获取带工具选项的响应
            ChatResponse chatResponse = getChatClient()
                    .prompt(prompt)
                    .system(getSystemPrompt()) // 因为prompt里面带着用户信息，故而传入系统信息
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应 用于Act 或者，就是观察结果
            this.toolCallChatResponse = chatResponse;
            // AI回复消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示信息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考：" + result);
            log.info(getName() + "选择了" + toolCallList.size() + "个工具来使用");
            // 记录工具调用的名称，如果工具为0，就返回空
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称: %s, 参数: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCallInfo.isEmpty()) {
                // 只有不调用工具时，才记录助手信息，并且设置false，因为没有工具可调用，所以就停止
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录，因为调用工具时会自动记录的。
                return true;
            }
        } catch (Exception e) {
            log.info(getName() + "思考过程中遇到了问题：" + e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理时遇到了错误：" + e.getMessage())
            );
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 行动的结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        // 记录消息上下文
        setMessageList(toolExecutionResult.conversationHistory());
        // 当前工具调用结果
        ToolResponseMessage toolResponseMessage
                = (ToolResponseMessage) toolExecutionResult.conversationHistory().getLast();
        String results = toolResponseMessage
                .getResponses()
                .stream()
                .map(response -> "工具" + response.name() + " 完成了它的任务！结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        // 判断是否调用了终止工具
        boolean terminatedToolCalled = toolResponseMessage
                .getResponses()
                .stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminatedToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;
    }
}
