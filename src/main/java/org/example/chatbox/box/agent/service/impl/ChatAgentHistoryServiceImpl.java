package org.example.chatbox.box.agent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.example.chatbox.box.agent.entity.ChatAgentHistory;
import org.example.chatbox.box.agent.mapper.ChatAgentHistoryMapper;
import org.example.chatbox.box.agent.service.ChatAgentHistoryService;
import org.example.chatbox.box.unity.enums.ChatHistoryMessageTypeEnum;
import org.example.chatbox.exception.ErrorCode;
import org.example.chatbox.exception.ThrowUtils;
import org.springframework.stereotype.Service;

/**
 * 智能体对话历史 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
public class ChatAgentHistoryServiceImpl extends ServiceImpl<ChatAgentHistoryMapper, ChatAgentHistory> implements ChatAgentHistoryService {

    /**
     *
     * @param agentId     智能体对话框id
     * @param message     消息 智能体回复的消息步骤有多个
     * @param messageType 消息类型 User AI
     * @return
     */
    @Override
    public boolean addAgentHistory(Long agentId, String message, String messageType) {
        // 验证消息是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null,
                ErrorCode.PARAMS_ERROR, "不支持类型的消息" + messageType);
        ChatAgentHistory chatAgentHistory = ChatAgentHistory.builder()
                .agentId(agentId)
                .message(message)
                .messageType(messageType)
                .build();

        return this.save(chatAgentHistory);
    }
}
