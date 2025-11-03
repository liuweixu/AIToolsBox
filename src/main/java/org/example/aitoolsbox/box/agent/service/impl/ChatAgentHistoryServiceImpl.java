package org.example.aitoolsbox.box.agent.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.box.agent.entity.ChatAgentHistory;
import org.example.aitoolsbox.box.agent.entity.ChatAgentHistoryQueryRequest;
import org.example.aitoolsbox.box.agent.mapper.ChatAgentHistoryMapper;
import org.example.aitoolsbox.box.agent.service.ChatAgentHistoryService;
import org.example.aitoolsbox.enums.ChatHistoryMessageTypeEnum;
import org.example.aitoolsbox.exception.ErrorCode;
import org.example.aitoolsbox.exception.ThrowUtils;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体对话历史 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
@Slf4j
public class ChatAgentHistoryServiceImpl extends ServiceImpl<ChatAgentHistoryMapper, ChatAgentHistory> implements ChatAgentHistoryService {

    /**
     * 新增智能体对话历史
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

    /**
     * 根据智能体 id 删除对话历史
     *
     * @param agentId
     * @return
     */
    @Override
    public boolean deleteChatHistoryByAgentId(Long agentId) {
        ThrowUtils.throwIf(agentId == null || agentId <= 0, ErrorCode.PARAMS_ERROR,
                "智能体对话框ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("agentId", agentId);
        return this.remove(queryWrapper);
    }

    /**
     * 分页查询某 智能体 的对话记录
     * 游标查询服务
     * this.getQueryWrapper里面就是游标分页查询方法
     *
     * @param agentId
     * @param pageSize
     * @param lastCreateTime
     * @return
     */
    @Override
    public Page<ChatAgentHistory> listChatHistoryByAgentId(Long agentId, int pageSize, LocalDateTime lastCreateTime) {
        ThrowUtils.throwIf(agentId == null || agentId <= 0, ErrorCode.PARAMS_ERROR, "智能体对话框ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 100, ErrorCode.PARAMS_ERROR, "智能体页面大小必须在1-100之间");
        // 构建查询条件
        ChatAgentHistoryQueryRequest chatAgentHistoryQueryRequest = new ChatAgentHistoryQueryRequest();
        chatAgentHistoryQueryRequest.setAgentId(agentId);
        chatAgentHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(chatAgentHistoryQueryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 加载智能体对话历史到内存
     *
     * @param agentId
     * @param chatMemory
     * @param maxCount   最多加载多少条
     * @return 加载成功的条数
     */
    @Override
    public int loadAgentHistoryToMemory(Long agentId, MessageWindowChatMemory chatMemory, int maxCount) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatAgentHistory::getAgentId, agentId)
                .orderBy(ChatAgentHistory::getCreateTime, false)
                .limit(1, maxCount);
        List<ChatAgentHistory> historyAgentList = this.list(queryWrapper);
        if (historyAgentList == null || historyAgentList.isEmpty()) {
            return 0;
        }
        // 反转列表，确保时间正序
        historyAgentList = historyAgentList.reversed();
        // 按时间顺序添加到记忆中
        int loadedCount = 0;
        // 先清理历史缓存，防止重复加载
        try {
            for (ChatAgentHistory chatAgentHistory : historyAgentList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(chatAgentHistory.getMessageType())) {
                    chatMemory.add(String.valueOf(agentId), new UserMessage(chatAgentHistory.getMessage()));
                    loadedCount += 1;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(chatAgentHistory.getMessageType())) {
                    chatMemory.add(String.valueOf(agentId), new AssistantMessage(chatAgentHistory.getMessage()));
                    loadedCount += 1;
                }
            }
            log.info("成功加载了agentId: {}，加载了 {} 条对话", agentId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败：agentId: {}, error: {}", agentId, e.getMessage());
            return 0;
        }
    }

    /**
     * 构造查询条件
     * 实现游标分页查询
     * 数据是按照createTime降序排列
     *
     * @param chatAgentHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatAgentHistoryQueryRequest chatAgentHistoryQueryRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (chatAgentHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatAgentHistoryQueryRequest.getId();
        Long agentId = chatAgentHistoryQueryRequest.getAgentId();
        String message = chatAgentHistoryQueryRequest.getMessage();
        String messageType = chatAgentHistoryQueryRequest.getMessageType();
        LocalDateTime lastCreateTime = chatAgentHistoryQueryRequest.getLastCreateTime();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("agentId", agentId)
                .eq("messageType", messageType);
        // 游标分页查询逻辑-只用createTime作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 按照时间降序排列
        queryWrapper.orderBy("createTime", false);
        return queryWrapper;
    }
}
