package org.example.chatbox.box.agent.controller;

import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.chatbox.box.agent.entity.ChatAgentHistory;
import org.example.chatbox.box.agent.service.ChatAgentHistoryService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 智能体对话历史 控制层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@RestController
@RequestMapping("/chatAgentHistory")
public class ChatAgentHistoryController {

    @Autowired
    private ChatAgentHistoryService chatAgentHistoryService;

    /**
     * 保存智能体对话历史。
     *
     * @param chatAgentHistory 智能体对话历史
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody ChatAgentHistory chatAgentHistory) {
        return chatAgentHistoryService.save(chatAgentHistory);
    }

    /**
     * 根据主键删除智能体对话历史。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return chatAgentHistoryService.removeById(id);
    }

    /**
     * 根据主键更新智能体对话历史。
     *
     * @param chatAgentHistory 智能体对话历史
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody ChatAgentHistory chatAgentHistory) {
        return chatAgentHistoryService.updateById(chatAgentHistory);
    }

    /**
     * 查询所有智能体对话历史。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ChatAgentHistory> list() {
        return chatAgentHistoryService.list();
    }

    /**
     * 根据主键获取智能体对话历史。
     *
     * @param id 智能体对话历史主键
     * @return 智能体对话历史详情
     */
    @GetMapping("getInfo/{id}")
    public ChatAgentHistory getInfo(@PathVariable Long id) {
        return chatAgentHistoryService.getById(id);
    }

    /**
     * 分页查询智能体对话历史。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<ChatAgentHistory> page(Page<ChatAgentHistory> page) {
        return chatAgentHistoryService.page(page);
    }

}
