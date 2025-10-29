package org.example.chatreact.chatmodel;

import org.example.chatreact.chatmodel.models.ChatModelStrategy;
import org.example.chatreact.enums.ChatModelEnum;
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
