package org.example.chatbox.box.unity.chat_history.service;

import com.mybatisflex.core.service.IService;
import org.example.chatbox.box.unity.chat_history.entity.ChatUnity;

import java.util.List;

/**
 * 对话框 服务层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
public interface ChatUnityService extends IService<ChatUnity> {

    /**
     * 创建对话框
     *
     * @return
     */
    public ChatUnity addChatUnity(String message);

    /**
     * 删除对话框
     *
     * @param id
     * @return
     */
    public boolean deleteChatUnityById(long id);

    /**
     * 获取对话框列表
     *
     * @return
     */
    public List<ChatUnity> getChatUnityList();

}
