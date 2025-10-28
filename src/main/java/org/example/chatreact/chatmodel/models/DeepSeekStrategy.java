package org.example.chatreact.chatmodel.models;

import jakarta.annotation.Resource;
import org.example.chatreact.chatmodel.AIPlatform;
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
        return AIPlatform.DEEPSEEK.name();
    }
}