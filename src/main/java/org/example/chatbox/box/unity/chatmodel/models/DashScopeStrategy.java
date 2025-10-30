package org.example.chatbox.box.unity.chatmodel.models;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.annotation.Resource;
import org.example.chatbox.box.unity.enums.ChatModelEnum;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class DashScopeStrategy implements ChatModelStrategy {

    @Resource
    private DashScopeChatModel dashScopeChatModel;


    @Override
    public ChatModel createChatModel() {
        return dashScopeChatModel;
    }

    @Override
    public String getPlatformName() {
        return ChatModelEnum.DASHSCOPE.name();
    }
}