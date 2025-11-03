package org.example.aitoolsbox.box.unity.chat_history.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.box.unity.app.UnityApp;
import org.example.aitoolsbox.box.unity.chat_history.entity.ChatUnity;
import org.example.aitoolsbox.box.unity.chat_history.mapper.ChatUnityMapper;
import org.example.aitoolsbox.box.unity.chat_history.service.ChatHistoryService;
import org.example.aitoolsbox.box.unity.chat_history.service.ChatUnityService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对话框 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
@Slf4j
public class ChatUnityServiceImpl extends ServiceImpl<ChatUnityMapper, ChatUnity> implements ChatUnityService {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UnityApp unityApp;

    /**
     * 创建对话框
     *
     * @return
     */
    @Override
    public ChatUnity addChatUnity(String message) {
        ChatUnity chatUnity = new ChatUnity();
        //TODO 想要实现大模型实时预测，但是思路我没想出来，也许要异步？
//        String response = unityApp.summaryResponse(message);
        String response = message.length() > 20 ? message.substring(0, 20) : message;
        chatUnity.setSummary(response);
        chatUnity.setIsSummary(1);
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
        try {
            chatHistoryService.deleteChatHistoryByUnityId(id);
        } catch (Exception e) {
            log.error("删除关联对话历史信息失败：{}", e.getMessage());
        }
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
