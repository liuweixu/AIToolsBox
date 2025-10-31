package org.example.chatbox.box.unity.chat_history.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import org.example.chatbox.box.unity.chat_history.entity.ChatHistory;
import org.example.chatbox.box.unity.chat_history.entity.ChatHistoryQueryRequest;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 添加对话历史
     *
     * @param unityId     对话框 id
     * @param message     消息
     * @param messageType 消息类型
     * @return 是否成功
     */
    boolean addChatHistory(Long unityId, String message, String messageType);

    /**
     * 根据对话框 id 删除对话历史
     *
     * @param unityId
     * @return
     */
    boolean deleteChatHistoryByUnityId(Long unityId);

    /**
     * 分页查询某 对话框 的对话记录
     *
     * @param unityId
     * @param pageSize
     * @param lastCreateTime
     * @return
     */
    Page<ChatHistory> listChatHistoryByUnityPage(Long unityId, int pageSize,
                                                 LocalDateTime lastCreateTime);

    /**
     * 加载对话历史到内存
     *
     * @param unityId
     * @param chatMemory
     * @param maxCount   最多加载多少条
     * @return 加载成功的条数
     */
    int loadChatHistoryToMemory(Long unityId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 构造查询条件
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
