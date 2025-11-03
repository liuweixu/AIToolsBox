package org.example.aitoolsbox.box.agent.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aitoolsbox.box.agent.entity.ChatAgent;

/**
 * 智能体对话框 映射层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Mapper
public interface ChatAgentMapper extends BaseMapper<ChatAgent> {

}
