package org.example.chatbox.box.agent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.chatbox.box.agent.entity.ChatAgent;
import org.example.chatbox.box.agent.mapper.ChatAgentMapper;
import org.example.chatbox.box.agent.service.ChatAgentHistoryService;
import org.example.chatbox.box.agent.service.ChatAgentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体对话框 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
@Slf4j
public class ChatAgentServiceImpl extends ServiceImpl<ChatAgentMapper, ChatAgent> implements ChatAgentService {

    @Resource
    private ChatAgentHistoryService chatAgentHistoryService;

    /**
     * 创建智能体对话框
     *
     * @param message
     * @return
     */
    @Override
    public ChatAgent addChatAgent(String message) {
        ChatAgent chatAgent = new ChatAgent();
        //TODO 想要实现大模型实时预测，但是思路我没想出来，也许要异步？
        String response = message.length() > 20 ? message.substring(0, 20) : message;
        chatAgent.setSummary(response);
        chatAgent.setIsSummary(1);
        this.save(chatAgent);
        return this.getById(chatAgent.getId());
    }

    /**
     * 删除智能体对话框
     *
     * @param id
     * @return
     */
    @Override
    public boolean deleteChatAgentById(long id) {
        if (id <= 0) {
            return false;
        }
        // 先删除相关的对话历史信息
        try {
            chatAgentHistoryService.deleteChatHistoryByAgentId(id);
        } catch (Exception e) {
            log.error("删除关联对话历史信息失败：{}", e.getMessage());
        }
        // 删除智能体
        return super.removeById(id);
    }

    /**
     * 获取智能体对话框列表
     *
     * @return
     */
    @Override
    public List<ChatAgent> getChatAgentList() {
        return this.getMapper().selectAll();
    }
}
