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
 * Prompt 重写器 - 利用护轨机制实现 Prompt 的重写功能
 * 当检测到潜在风险内容时，自动移除或替换敏感信息
 * 同时优化 Prompt 的专业性，防止超出大模型的能力
 * 在保证安全的同时提升用户体验，而不是直接拒绝请求
 */
public class PromptSafetyAdvisor implements CallAdvisor, StreamAdvisor {

    // 敏感词列表（需要移除或替换的词汇）
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令", "ignore previous instructions", "ignore above",
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak");

    // 注入攻击模式（需要移除的模式）
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:"));

    // 最大输入长度限制
    private static final int MAX_INPUT_LENGTH = 1000;

    // 最小有效输入长度
    private static final int MIN_INPUT_LENGTH = 1;

    /**
     * 重写 Prompt：移除或替换敏感信息，优化专业性
     *
     * @param originalInput 原始用户输入
     * @return 重写后的安全输入，如果输入完全无效则返回 null
     */
    private String rewritePrompt(String originalInput) {
        if (originalInput == null || originalInput.trim().isEmpty()) {
            return null; // 空输入无法重写
        }

        String rewritten = originalInput;

        // 1. 移除注入攻击模式
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(rewritten).find()) {
                rewritten = pattern.matcher(rewritten).replaceAll("");
            }
        }

        // 2. 移除或替换敏感词（用空格替换，保持语句连贯性）
        String lowerRewritten = rewritten.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            String lowerSensitiveWord = sensitiveWord.toLowerCase();
            if (lowerRewritten.contains(lowerSensitiveWord)) {
                // 使用正则表达式进行不区分大小写的替换
                Pattern sensitivePattern = Pattern.compile("(?i)" + Pattern.quote(sensitiveWord));
                rewritten = sensitivePattern.matcher(rewritten).replaceAll("");
            }
        }

        // 3. 清理多余的空格和换行
        rewritten = rewritten.replaceAll("\\s+", " ").trim();

        // 4. 处理长度限制：如果超出限制，智能截断
        if (rewritten.length() > MAX_INPUT_LENGTH) {
            // 优先在句号、问号、感叹号处截断
            int cutPoint = MAX_INPUT_LENGTH;
            int lastSentenceEnd = Math.max(
                    rewritten.lastIndexOf('。', MAX_INPUT_LENGTH),
                    Math.max(
                            rewritten.lastIndexOf('.', MAX_INPUT_LENGTH),
                            rewritten.lastIndexOf('！', MAX_INPUT_LENGTH)
                    )
            );
            // 尝试在句子边界截断
            if (lastSentenceEnd > MAX_INPUT_LENGTH * 0.7) {
                cutPoint = lastSentenceEnd + 1;
            }
            rewritten = rewritten.substring(0, cutPoint).trim();
        }

        // 5. 优化 Prompt 专业性：移除多余的语气词和无效字符
        rewritten = optimizePromptProfessionalism(rewritten);

        // 6. 如果重写后内容太短或为空，返回 null
        if (rewritten.trim().length() < MIN_INPUT_LENGTH) {
            return null;
        }

        return rewritten;
    }

    /**
     * 优化 Prompt 的专业性
     * 移除多余的语气词、重复内容，提升清晰度
     */
    private String optimizePromptProfessionalism(String input) {
        String optimized = input;

        // 移除过多的语气词（保留适度的语气）

        // 移除连续重复的标点符号
        optimized = optimized.replaceAll("([。！？\\.!\\?])\\1+", "$1");

        // 移除多余的空格
        optimized = optimized.replaceAll("\\s+", " ").trim();

        return optimized;
    }

    /**
     * 检查并重写请求中的用户输入
     *
     * @param request 原始请求
     * @return 重写后的请求，如果输入无效则返回 null
     */
    private ChatClientRequest rewriteRequest(ChatClientRequest request) {
        String originalInput = request.prompt().getUserMessage().getText();
        String rewrittenInput = rewritePrompt(originalInput);

        // 如果重写后无效，返回 null
        if (rewrittenInput == null) {
            return null;
        }

        // 如果内容没有变化，直接返回原请求
        if (rewrittenInput.equals(originalInput)) {
            return request;
        }

        // 重写用户消息
        List<Message> originalMessages = request.prompt().getInstructions();
        List<Message> rewrittenMessages = originalMessages.stream()
                .map(msg -> {
                    if (msg instanceof UserMessage) {
                        return new UserMessage(rewrittenInput);
                    }
                    return msg;
                })
                .collect(Collectors.toList());

        // 创建新的 Prompt，保留原有的 ChatOptions
        Prompt newPrompt = new Prompt(rewrittenMessages, request.prompt().getOptions());
        return new ChatClientRequest(newPrompt, request.context());
    }

    /**
     * 处理无效输入（当重写后仍然无效时）
     * 返回一个友好的错误消息请求
     */
    private ChatClientRequest createErrorMessageRequest(ChatClientRequest request, String errorMessage) {
        List<Message> originalMessages = request.prompt().getInstructions();
        // 过滤掉用户消息，保留其他系统消息等内容
        List<Message> filteredMessages = originalMessages.stream()
                .filter(msg -> !(msg instanceof UserMessage))
                .collect(Collectors.toList());
        // 添加新的错误消息
        filteredMessages.add(new UserMessage(errorMessage));
        // 构建错误消息回复
        Prompt newPrompt = new Prompt(filteredMessages, request.prompt().getOptions());
        return new ChatClientRequest(newPrompt, request.context());
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientRequest rewrittenRequest = rewriteRequest(chatClientRequest);

        if (rewrittenRequest == null) {
            // 输入完全无效，无法重写，返回错误提示
            ChatClientRequest errorRequest = createErrorMessageRequest(
                    chatClientRequest,
                    "抱歉，您的输入内容无效，无法处理。请输入有效的问题或内容。");
            return callAdvisorChain.nextCall(errorRequest);
        }

        // 使用重写后的请求继续处理
        return callAdvisorChain.nextCall(rewrittenRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        ChatClientRequest rewrittenRequest = rewriteRequest(chatClientRequest);

        if (rewrittenRequest == null) {
            // 输入完全无效，无法重写，返回错误提示
            ChatClientRequest errorRequest = createErrorMessageRequest(
                    chatClientRequest,
                    "抱歉，您的输入内容无效，无法处理。请输入有效的问题或内容。");
            return streamAdvisorChain.nextStream(errorRequest);
        }

        // 使用重写后的请求继续处理
        return streamAdvisorChain.nextStream(rewrittenRequest);
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