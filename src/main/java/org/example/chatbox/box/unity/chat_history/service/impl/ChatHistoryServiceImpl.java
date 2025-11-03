package org.example.chatbox.box.unity.chat_history.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.chatbox.box.unity.chat_history.entity.ChatHistory;
import org.example.chatbox.box.unity.chat_history.entity.ChatHistoryQueryRequest;
import org.example.chatbox.box.unity.chat_history.mapper.ChatHistoryMapper;
import org.example.chatbox.box.unity.chat_history.service.ChatHistoryService;
import org.example.chatbox.enums.ChatHistoryMessageTypeEnum;
import org.example.chatbox.exception.ErrorCode;
import org.example.chatbox.exception.ThrowUtils;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {
    /**
     * 新增对话历史
     *
     * @param unityId     对话框 id
     * @param message     消息
     * @param messageType 消息类型
     * @return
     */
    @Override
    public boolean addChatHistory(Long unityId, String message, String messageType) {

        // 验证消息是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null,
                ErrorCode.PARAMS_ERROR, "不支持类型的消息" + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .unityId(unityId)
                .message(message)
                .messageType(messageType)
                .build();
        return this.save(chatHistory);
    }

    /**
     * 根据对话框 id 删除对话历史
     *
     * @param unityId
     * @return
     */
    @Override
    public boolean deleteChatHistoryByUnityId(Long unityId) {
        ThrowUtils.throwIf(unityId == null || unityId <= 0, ErrorCode.PARAMS_ERROR,
                "对话框ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("unityId", unityId);
        return this.remove(queryWrapper);
    }


    /**
     * 分页查询某一个对话框的对话记录
     * 游标查询服务
     * this.getQueryWrapper里面就是游标分页查询方法
     *
     * @param unityId
     * @param pageSize
     * @param lastCreateTime
     * @return
     */
    @Override
    public Page<ChatHistory> listChatHistoryByUnityId(Long unityId, int pageSize, LocalDateTime lastCreateTime) {
        ThrowUtils.throwIf(unityId == null || unityId <= 0, ErrorCode.PARAMS_ERROR, "对话框ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 100, ErrorCode.PARAMS_ERROR, "页面大小必须在1-100之间");
        // 构建查询条件
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setUnityId(unityId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 加载对话历史到记忆中
     *
     * @param unityId
     * @param chatMemory
     * @param maxCount   最多加载多少条
     * @return 加载成功的条数
     */
    @Override
    public int loadChatHistoryToMemory(Long unityId, MessageWindowChatMemory chatMemory, int maxCount) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getUnityId, unityId)
                .orderBy(ChatHistory::getCreateTime, false)
                .limit(1, maxCount);
        List<ChatHistory> historyList = this.list(queryWrapper);
        if (historyList == null || historyList.isEmpty()) {
            return 0;
        }
        // 反转列表，确保时间正序
        historyList = historyList.reversed();
        // 按时间顺序添加到记忆中
        int loadedCount = 0;
        // 先清理历史缓存，防止重复加载
        chatMemory.clear(String.valueOf(unityId));
        try {
            for (ChatHistory chatHistory : historyList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(String.valueOf(unityId), new UserMessage(chatHistory.getMessage()));
                    loadedCount += 1;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(String.valueOf(unityId), new AssistantMessage(chatHistory.getMessage()));
                    loadedCount += 1;
                }
            }
            log.info("成功加载了unityId: {}，加载了 {} 条对话", unityId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败：unityId: {}, error: {}", unityId, e.getMessage());
            return 0;
        }
    }

    /**
     * 构造查询条件
     * 实现游标分页查询
     * 数据是按照createTime降序排列
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        Long unityId = chatHistoryQueryRequest.getUnityId();
        String messageType = chatHistoryQueryRequest.getMessageType();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("unityId", unityId)
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
