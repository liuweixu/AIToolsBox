package org.example.chatbox.box.unity.controller;

import jakarta.annotation.Resource;
import org.example.chatbox.box.unity.chat_history.entity.ChatRequest;
import org.example.chatbox.box.unity.chat_history.entity.ChatUnity;
import org.example.chatbox.box.unity.chat_history.service.ChatUnityService;
import org.example.chatbox.box.unity.common.BaseResponse;
import org.example.chatbox.box.unity.common.ResultUtils;
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

    @GetMapping("/create")
    public BaseResponse<ChatUnity> addChatApp() {
        return ResultUtils.success(chatUnityService.addChatUnity());
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
