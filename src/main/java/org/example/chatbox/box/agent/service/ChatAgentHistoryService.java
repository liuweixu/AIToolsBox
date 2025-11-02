package org.example.chatbox.box.agent.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import org.example.chatbox.box.agent.entity.ChatAgentHistory;
import org.example.chatbox.box.agent.entity.ChatAgentHistoryQueryRequest;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import java.time.LocalDateTime;

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


    /**
     * 根据智能体 id 删除对话历史
     *
     * @param agentId
     * @return
     */
    boolean deleteChatHistoryByAgentId(Long agentId);

    /**
     * 分页查询某 智能体 的对话记录
     *
     * @param agentId
     * @param pageSize
     * @param lastCreateTime
     * @return
     */
    Page<ChatAgentHistory> listChatHistoryByAgentId(Long agentId, int pageSize,
                                                    LocalDateTime lastCreateTime);

    /**
     * 加载智能体对话历史到内存
     *
     * @param agentId
     * @param chatMemory
     * @param maxCount   最多加载多少条
     * @return 加载成功的条数
     */
    int loadAgentHistoryToMemory(Long agentId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 构造查询条件
     *
     * @param chatAgentHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatAgentHistoryQueryRequest chatAgentHistoryQueryRequest);
}
