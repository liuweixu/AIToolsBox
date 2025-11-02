package org.example.chatbox.box.agent.controller;

import jakarta.annotation.Resource;
import org.example.chatbox.box.agent.entity.ChatAgentHistory;
import org.example.chatbox.box.agent.model.AgentApp;
import org.example.chatbox.box.agent.service.ChatAgentHistoryService;
import org.example.chatbox.box.unity.enums.ChatHistoryMessageTypeEnum;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/agent/manus")
public class AIManusController {

    @Resource
    private AgentApp agentApp;

    @Resource
    private ChatAgentHistoryService chatAgentHistoryService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManus(
            @RequestParam String message,
            @RequestParam String agentId,
            @RequestParam String modelName) {
        chatAgentHistoryService.addAgentHistory(Long.valueOf(agentId), message, ChatHistoryMessageTypeEnum.USER.getValue());
        return agentApp.doChatWithManus(message, agentId, modelName);
    }
}
