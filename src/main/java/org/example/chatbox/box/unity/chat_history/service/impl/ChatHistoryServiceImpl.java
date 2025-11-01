package org.example.chatbox.box.unity.chat_history.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.example.chatbox.box.unity.chat_history.entity.ChatHistory;
import org.example.chatbox.box.unity.chat_history.entity.ChatHistoryQueryRequest;
import org.example.chatbox.box.unity.chat_history.mapper.ChatHistoryMapper;
import org.example.chatbox.box.unity.chat_history.service.ChatHistoryService;
import org.example.chatbox.box.unity.enums.ChatHistoryMessageTypeEnum;
import org.example.chatbox.exception.ErrorCode;
import org.example.chatbox.exception.ThrowUtils;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
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
     *
     * @param unityId
     * @param pageSize
     * @param lastCreateTime
     * @return
     */
    @Override
    public Page<ChatHistory> listChatHistoryByUnityPage(Long unityId, int pageSize, LocalDateTime lastCreateTime) {
        ThrowUtils.throwIf(unityId == null || unityId <= 0, ErrorCode.PARAMS_ERROR, "对话框ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        // 构建查询条件
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setUnityId(unityId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 加载对话历史到内存
     *
     * @param unityId
     * @param chatMemory
     * @param maxCount   最多加载多少条
     * @return 加载成功的条数
     */
    @Override
    public int loadChatHistoryToMemory(Long unityId, MessageWindowChatMemory chatMemory, int maxCount) {
        return 0;
    }

    /**
     * 构造查询条件
     * 实现游标分页查询
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
