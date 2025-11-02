package org.example.chatbox.box.agent.service;

import com.mybatisflex.core.service.IService;
import org.example.chatbox.box.agent.entity.ChatAgentHistory;

/**
 * 智能体对话历史 服务层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
public interface ChatAgentHistoryService extends IService<ChatAgentHistory> {

    /**
     *
     * @param agentId     智能体对话框id
     * @param message     消息 智能体回复的消息步骤有多个
     * @param messageType 消息类型 User AI
     * @return
     */
    public boolean addAgentHistory(Long agentId, String message, String messageType);
}
