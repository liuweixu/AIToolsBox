package org.example.aitoolsbox.box.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aitoolsbox.box.agent.entity.ChatAgentHistory;

/**
 * 智能体对话历史 映射层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Mapper
public interface ChatAgentHistoryMapper extends BaseMapper<ChatAgentHistory> {

}
