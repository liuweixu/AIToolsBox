package org.example.chatbox.box.agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.chatbox.box.agent.enums.AgentState;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并执行下一步行动
     *
     * @return 是否需要行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行行动
     *
     * @return 行动的结果
     */
    public abstract String act();

    /**
     * <p>
     * 执行单个步骤: 思考->行动
     *
     * @return 步骤执行的结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成——无需行动";
            }
            return act();
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "ReAct执行失败" + e.getMessage();
        }
    }
}
