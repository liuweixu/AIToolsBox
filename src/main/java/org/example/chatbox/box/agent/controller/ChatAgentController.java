package org.example.chatbox.box.agent.controller;

import jakarta.annotation.Resource;
import org.example.chatbox.box.agent.entity.ChatAgentMessage;
import org.example.chatbox.box.agent.entity.ChatAgentRequest;
import org.example.chatbox.common.BaseResponse;
import org.example.chatbox.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.example.chatbox.box.agent.entity.ChatAgent;
import org.example.chatbox.box.agent.service.ChatAgentService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 智能体对话框 控制层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@RestController
@RequestMapping("/agent/manus")
public class ChatAgentController {

    @Resource
    private ChatAgentService chatAgentService;

    @PostMapping("/create")
    public BaseResponse<ChatAgent> addChatAgent(@RequestBody ChatAgentMessage chatAgentMessage) {
        return ResultUtils.success(chatAgentService.addChatAgent(chatAgentMessage.getMessage()));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChatAgent(@RequestBody ChatAgentRequest chatAgentRequest) {
        long agentId = Long.parseLong(chatAgentRequest.getId());
        return ResultUtils.success(chatAgentService.deleteChatAgentById(agentId));
    }

    @GetMapping("/list")
    public BaseResponse<List<ChatAgent>> getChatAgentList() {
        return ResultUtils.success(chatAgentService.getChatAgentList());
    }

}
