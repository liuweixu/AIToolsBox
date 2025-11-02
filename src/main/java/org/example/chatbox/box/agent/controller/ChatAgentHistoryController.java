package org.example.chatbox.box.agent.controller;

import com.mybatisflex.core.paginate.Page;
import org.example.chatbox.common.BaseResponse;
import org.example.chatbox.common.ResultUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.chatbox.box.agent.entity.ChatAgentHistory;
import org.example.chatbox.box.agent.service.ChatAgentHistoryService;

import java.time.LocalDateTime;

/**
 * 智能体对话历史 控制层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@RestController
@RequestMapping("/agent/history")
public class ChatAgentHistoryController {

    @Autowired
    private ChatAgentHistoryService chatAgentHistoryService;

    @GetMapping("/{agentId}")
    public BaseResponse<Page<ChatAgentHistory>> listChatHistoryByAgentId(
            @PathVariable Long agentId,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(required = false) LocalDateTime lastCreateTime) {
        return ResultUtils.success(chatAgentHistoryService.listChatHistoryByAgentId(
                agentId, pageSize, lastCreateTime
        ));
    }
}
