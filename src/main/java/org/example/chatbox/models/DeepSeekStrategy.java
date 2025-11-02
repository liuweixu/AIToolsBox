package org.example.chatbox.models;

import jakarta.annotation.Resource;
import org.example.chatbox.enums.ChatModelEnum;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;

@Service
public class DeepSeekStrategy implements ChatModelStrategy {

    @Resource
    private DeepSeekChatModel deepSeekChatModel;


    @Override
    public ChatModel createChatModel() {
        return deepSeekChatModel;
    }

    @Override
    public String getPlatformName() {
        return ChatModelEnum.DEEPSEEK.name();
    }
}