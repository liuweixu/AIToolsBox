package org.example.chatreact.chatmodel.models;

import jakarta.annotation.Resource;
import org.example.chatreact.chatmodel.ChatModelEnum;
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