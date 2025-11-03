package org.example.aitoolsbox.box.agent.service;

import com.mybatisflex.core.service.IService;
import org.example.aitoolsbox.box.agent.entity.ChatAgent;

import java.util.List;

/**
 * 智能体对话框 服务层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
public interface ChatAgentService extends IService<ChatAgent> {

    /**
     * 创建智能体对话框
     *
     * @return
     */
    public ChatAgent addChatAgent(String message);

    /**
     * 删除智能体对话框
     *
     * @param id
     * @return
     */
    public boolean deleteChatAgentById(long id);

    /**
     * 获取智能体对话框列表
     *
     * @return
     */
    public List<ChatAgent> getChatAgentList();
}
