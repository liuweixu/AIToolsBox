package org.example.chatbox.box.unity.chat_model.models;

import org.springframework.ai.chat.model.ChatModel;

public interface ChatModelStrategy {
    ChatModel createChatModel();

    String getPlatformName();
}
