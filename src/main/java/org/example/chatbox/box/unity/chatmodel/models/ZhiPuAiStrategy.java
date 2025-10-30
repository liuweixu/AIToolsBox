package org.example.chatbox.box.unity.chatmodel.models;

import jakarta.annotation.Resource;
import org.example.chatbox.box.unity.enums.ChatModelEnum;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class ZhiPuAiStrategy implements ChatModelStrategy {

    @Resource
    private ZhiPuAiChatModel zhiPuAiChatModel;


    @Override
    public ChatModel createChatModel() {
        return zhiPuAiChatModel;
    }

    @Override
    public String getPlatformName() {
        return ChatModelEnum.ZHIPUAI.name();
    }
}