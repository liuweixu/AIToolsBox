package org.example.chatbox.box.unity.controller;

import com.mybatisflex.core.paginate.Page;
import org.example.chatbox.box.unity.chat_history.entity.ChatHistory;
import org.example.chatbox.box.unity.chat_history.service.ChatHistoryService;
import org.example.chatbox.box.unity.common.BaseResponse;
import org.example.chatbox.box.unity.common.ResultUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@RestController
@RequestMapping("/unity/history")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @GetMapping("/{unityId}")
    public BaseResponse<Page<ChatHistory>> listChatHistoryByUnityId(
            @PathVariable Long unityId,
            @RequestParam(defaultValue = "10") int PageSize,
            @RequestParam(required = false) LocalDateTime lastCreateTime) {
        return ResultUtils.success(chatHistoryService.listChatHistoryByUnityId(
                unityId, PageSize, lastCreateTime
        ));
    }

}
