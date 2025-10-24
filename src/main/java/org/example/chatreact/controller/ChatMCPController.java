package org.example.chatreact.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/ai")
@Slf4j
public class ChatMCPController {

    private final ChatClient chatClient;

    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;

    public ChatMCPController(ChatClient.Builder chatClientBuilder,
                             ChatMemory chatMemory,
                             SyncMcpToolCallbackProvider syncMcpToolCallbackProvider) {
        // 会话记忆也是使用Advisor实现的，Advisor定位可以类比拦截器
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(memoryAdvisor)
                .build();
        this.syncMcpToolCallbackProvider = syncMcpToolCallbackProvider;
    }

    /**
     * 只引入MCP
     * @param memoryId
     * @param message
     * @return
     */
    @GetMapping(value = "/mcp")
    public Flux<ServerSentEvent<String>> chat(
            @RequestParam int memoryId,
            @RequestParam String message) {
        log.info("[ai/mcp] memoryId:{} message:{}", memoryId, message);
        return this.chatClient.prompt()
                .toolCallbacks(this.syncMcpToolCallbackProvider)
                .user(message)
                // 维护多轮对话
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, memoryId))
                .stream()
                .content()
                .map( chuck -> ServerSentEvent.<String>builder()
                        .data(chuck)
                        .build());
    }
}
