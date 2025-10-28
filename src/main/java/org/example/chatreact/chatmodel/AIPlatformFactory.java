package org.example.chatreact.chatmodel;

import org.example.chatreact.chatmodel.models.ChatModelStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AIPlatformFactory {

    private final Map<String, ChatModelStrategy> strategyMap = new HashMap<>();

    public AIPlatformFactory(List<ChatModelStrategy> strategies) {
        for (ChatModelStrategy strategy : strategies) {
            strategyMap.put(strategy.getPlatformName(), strategy);
        }
    }

    public ChatModel getChatModel(String aiPlatform) {
        ChatModelStrategy strategy = strategyMap.getOrDefault(
                aiPlatform,
                strategyMap.get(AIPlatform.OLLAMA.name()) // 默认
        );
        return strategy.createChatModel();
    }
}
