package org.example.aitoolsbox.box.unity.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 自定义输入拦截器。
 * 类似LangChain4j的输入护轨
 * 当检测到输入异常时，会修改请求让AI在对话界面直接回复相应的错误内容
 */
public class PromptSafetyAdvisor implements CallAdvisor, StreamAdvisor {

    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令", "ignore previous instructions", "ignore above",
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak");

    // 注入攻击模式
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:"));

    /**
     * 验证输入并返回错误消息，如果没有错误则返回null
     */
    private String validateInput(ChatClientRequest request) {
        String input = request.prompt().getUserMessage().getText();
        // 检查输入长度
        if (input.length() > 1000) {
            return "抱歉，您的输入内容过长，超过了 1000 字符的限制。请缩短内容后重试。";
        }
        // 检查是否为空
        if (input.trim().isEmpty()) {
            return "抱歉，输入内容不能为空，请输入有效的问题或内容。";
        }
        // 检查敏感词
        String lowerInput = input.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerInput.contains(sensitiveWord.toLowerCase())) {
                return "抱歉，您的输入包含不当内容，为了维护良好的对话环境，请修改后重试。";
            }
        }
        // 检查注入攻击模式
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return "抱歉，检测到您的输入可能存在安全风险，请求已被拒绝。请使用正常的问题进行交流。";
            }
        }
        return null; // 没有错误
    }

    /**
     * 修改请求，将用户输入替换为错误消息，让AI自然回复错误消息
     */
    private ChatClientRequest replaceUserInputWithErrorMessage(ChatClientRequest request, String errorMessage) {
        // 获取原始 Prompt 的所有消息
        List<Message> originalMessages = request.prompt().getInstructions();
        // 过滤掉用户消息，保留系统消息等其他消息
        List<Message> filteredMessages = originalMessages.stream()
                .filter(msg -> !(msg instanceof UserMessage))
                .collect(Collectors.toList());
        // 添加新的错误消息作为用户消息
        filteredMessages.add(new UserMessage(errorMessage));
        // 创建新的 Prompt，保留原有的 ChatOptions
        Prompt newPrompt = new Prompt(filteredMessages, request.prompt().getOptions());
        return new ChatClientRequest(newPrompt, request.context());
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        String errorMessage = validateInput(chatClientRequest);
        if (errorMessage != null) {
            // 如果有错误，修改请求让AI回复错误消息
            ChatClientRequest modifiedRequest = replaceUserInputWithErrorMessage(chatClientRequest, errorMessage);
            return callAdvisorChain.nextCall(modifiedRequest);
        }
        // 没有错误，继续正常流程
        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        String errorMessage = validateInput(chatClientRequest);
        if (errorMessage != null) {
            // 如果有错误，修改请求让AI回复错误消息
            ChatClientRequest modifiedRequest = replaceUserInputWithErrorMessage(chatClientRequest, errorMessage);
            return streamAdvisorChain.nextStream(modifiedRequest);
        }
        // 没有错误，继续正常流程
        return streamAdvisorChain.nextStream(chatClientRequest);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
