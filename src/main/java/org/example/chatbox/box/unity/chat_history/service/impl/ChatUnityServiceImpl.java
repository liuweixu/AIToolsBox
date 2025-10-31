package org.example.chatbox.box.unity.chat_history.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.chatbox.box.unity.chat_history.entity.ChatUnity;
import org.example.chatbox.box.unity.chat_history.mapper.ChatUnityMapper;
import org.example.chatbox.box.unity.chat_history.service.ChatHistoryService;
import org.example.chatbox.box.unity.chat_history.service.ChatUnityService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对话框 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
public class ChatUnityServiceImpl extends ServiceImpl<ChatUnityMapper, ChatUnity> implements ChatUnityService {


    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 创建对话框
     *
     * @return
     */
    @Override
    public ChatUnity addChatUnity() {
        ChatUnity chatUnity = new ChatUnity();
        this.save(chatUnity);
        return this.getById(chatUnity.getId());
    }

    /**
     * 删除对话框
     *
     * @param id
     * @return
     */
    @Override
    public boolean deleteChatUnityById(long id) {
        if (id <= 0) {
            return false;
        }
        // 先删除相关的对话历史信息
//        try {
//            chatHistoryService.deleteChatHistoryByUnityId(id);
//        } catch (Exception e) {
//            log.error("删除关联对话历史信息失败：{}", e.getMessage());
//        }
        // 删除应用
        return super.removeById(id);
    }

    /**
     * 获取对话列表
     *
     * @return
     */
    @Override
    public List<ChatUnity> getChatUnityList() {
        return this.getMapper().selectAll();
    }
}
