package org.example.aitoolsbox.box.unity.controller;

import jakarta.annotation.Resource;
import org.example.aitoolsbox.box.unity.chat_history.entity.ChatMessage;
import org.example.aitoolsbox.box.unity.chat_history.entity.ChatRequest;
import org.example.aitoolsbox.box.unity.chat_history.entity.ChatUnity;
import org.example.aitoolsbox.box.unity.chat_history.service.ChatUnityService;
import org.example.aitoolsbox.common.BaseResponse;
import org.example.aitoolsbox.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 对话框 控制层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@RestController
@RequestMapping("/unity/chat")
public class ChatUnityController {


    @Resource
    private ChatUnityService chatUnityService;

    @PostMapping("/create")
    public BaseResponse<ChatUnity> addChatApp(@RequestBody ChatMessage chatMessage) {
        return ResultUtils.success(chatUnityService.addChatUnity(chatMessage.getMessage()));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChatApp(@RequestBody ChatRequest chatRequest) {
        long unityId = Long.parseLong(chatRequest.getId());
        return ResultUtils.success(chatUnityService.deleteChatUnityById(unityId));
    }

    @GetMapping("/list")
    public BaseResponse<List<ChatUnity>> getChatUnityList() {
        return ResultUtils.success(chatUnityService.getChatUnityList());
    }
}
