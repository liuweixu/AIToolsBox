package org.example.aitoolsbox.models;

import jakarta.annotation.Resource;
import org.example.aitoolsbox.enums.ChatModelEnum;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
public class OllamaStrategy implements ChatModelStrategy {

    @Resource
    private OllamaChatModel ollamaChatModel;


    @Override
    public ChatModel createChatModel() {
        return ollamaChatModel;
    }

    @Override
    public String getPlatformName() {
        return ChatModelEnum.OLLAMA.name();
    }
}