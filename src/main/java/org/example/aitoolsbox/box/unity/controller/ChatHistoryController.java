package org.example.aitoolsbox.box.unity.controller;

import com.mybatisflex.core.paginate.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.box.unity.chat_history.entity.ChatHistory;
import org.example.aitoolsbox.box.unity.chat_history.service.ChatHistoryService;
import org.example.aitoolsbox.common.BaseResponse;
import org.example.aitoolsbox.common.ResultUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@RestController
@RequestMapping("/unity/history")
@Slf4j
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
