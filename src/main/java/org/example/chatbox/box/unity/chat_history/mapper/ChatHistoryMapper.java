package org.example.chatbox.box.unity.chat_history.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.chatbox.box.unity.chat_history.entity.ChatHistory;

/**
 * 对话历史 映射层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

}
