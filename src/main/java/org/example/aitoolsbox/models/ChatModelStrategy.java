package org.example.aitoolsbox.models;

import org.springframework.ai.chat.model.ChatModel;

public interface ChatModelStrategy {
    ChatModel createChatModel();

    String getPlatformName();
}
