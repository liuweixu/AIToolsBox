package org.example.chatbox.box.unity.chat_model;

import org.example.chatbox.box.unity.chat_model.models.ChatModelStrategy;
import org.example.chatbox.box.unity.enums.ChatModelEnum;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatModelFactory {

    private final Map<String, ChatModelStrategy> strategyMap = new HashMap<>();

    public ChatModelFactory(List<ChatModelStrategy> strategies) {
        for (ChatModelStrategy strategy : strategies) {
            strategyMap.put(strategy.getPlatformName(), strategy);
        }
    }

    public ChatModel getChatModel(String modelName) {
        ChatModelStrategy strategy = strategyMap.getOrDefault(
                modelName,
                strategyMap.get(ChatModelEnum.OLLAMA.name()) // 默认
        );
        return strategy.createChatModel();
    }
}
