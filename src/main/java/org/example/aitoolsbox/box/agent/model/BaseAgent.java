package org.example.aitoolsbox.box.agent.model;

import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.enums.AgentState;
import org.example.aitoolsbox.box.agent.service.ChatAgentHistoryService;
import org.example.aitoolsbox.enums.ChatHistoryMessageTypeEnum;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态 默认空闲状态
    // TODO 利用State属性控制智能体的执行流程
    private AgentState state = AgentState.IDLE;

    // 执行控制
    int currentStep = 0;
    int maxStep = 10;

    // LLM
    private ChatClient chatClient;

    // Memory 需要自主维护上下文
    private List<Message> messageList = new ArrayList<>();

    // 添加循环检测，防止陷入无限循环
    private int duplicateThreshold = 2;

    @Resource
    private ChatAgentHistoryService chatAgentHistoryService;

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StringUtil.isEmpty(userPrompt)) {
            throw new RuntimeException("Cannot run agent from empty user prompt");
        }
        // 更改状态
        state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxStep);
                // 单步执行
                String stepResult = step();
                // 判断是否陷入无限循环
                if (this.isStuck()) {
                    this.handleStuckState();
                }
                String result = "Step " + stepNumber + ":" + stepResult;
                results.add(result); // 存储每一步的结果
            }
            // 检查步数是否超出限制
            if (currentStep >= maxStep) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max step %d".formatted(maxStep));
            }
            return String.join("\n", results); // 将所有步的结果结合
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 使用SSE技术将智能体的推理过程实时分步输出给用户
     *
     * @param userPrompt
     * @return
     */
    public SseEmitter runStream(String userPrompt, Long agentId) {
        // 创建SseEmitter，设置较长的超时时间
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        // 使用线程异步处理，避免阻塞进程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误，无法从状态运行智能体：" + this.state);
                    emitter.complete();
                    return;
                }
                if (StringUtil.isEmpty(userPrompt)) {
                    emitter.send("错误：不能用空提示词运行智能体");
                    emitter.complete();
                    return;
                }

                // 更改状态
                state = AgentState.RUNNING;
                // 记录消息上下文
                messageList.add(new UserMessage(userPrompt));

                try {
                    for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step " + stepNumber + "/" + maxStep);
                        // 单步执行
                        String stepResult = step();
                        String result = "Step " + stepNumber + ":" + stepResult + "\n\n";
                        if (this.isStuck()) {
                            this.handleStuckState();
                        }
                        // 发送每一步的结果
                        chatAgentHistoryService.addAgentHistory(agentId, result, ChatHistoryMessageTypeEnum.AI.getValue());
                        emitter.send(result);
                    }
                    // 检查是否超出步骤的限制
                    if (currentStep >= maxStep) {
                        state = AgentState.FINISHED;
                        emitter.send("执行结束：达到最大步骤%d".formatted(maxStep));
                    }
                    // 正常完成
                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("执行智能体失败", e);
                    try {
                        emitter.send("执行错误：" + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE连接超时");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE连接结束");
        });
        return emitter;
    }

    /**
     * 抽象方法：没有方法体的方法被称为抽象方法，如果有抽象方法，那该方法所在的类必须是抽象类
     * <p>
     * 执行单个步骤
     *
     * @return 步骤执行的结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {

    }

    /**
     * 处理陷入循环状态
     */
    protected void handleStuckState() {
        String stuckPrompt = "观察到重复响应。考虑新策略，避免重复已尝试过的无效路径";
        this.nextStepPrompt = stuckPrompt + "\n" +
                (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        setNextStepPrompt(this.nextStepPrompt);
        System.out.println("AgentApp detected stuck state. Added Prompt " + stuckPrompt);

        // 不终止状态，保持 RUNNING
        if (this.state == AgentState.RUNNING) {
            // 可以考虑插入一个提示到 messageList，让下一轮继续
            this.messageList.add(new UserMessage(this.nextStepPrompt));
        }
    }

    /**
     * 检查智能体是否陷入循环
     *
     * @return 是否陷入循环
     */
    protected boolean isStuck() {
        List<Message> messages = this.getMessageList();
        if (messages.size() < 2) {
            return false;
        }

        Message lastMessage = messages.get(messages.size() - 1);
        if (lastMessage.getText() == null || lastMessage.getText().isEmpty()) {
            return false;
        }

        // 计算重复内容次数
        // 就是按照最后一个消息是否与前面的消息存在重复
        int duplicateCount = 0;
        for (int i = messages.size() - 2; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg.getMessageType() == MessageType.ASSISTANT
                    && lastMessage.getText().equals(msg.getText())) {
                duplicateCount++;
            }
        }

        return duplicateCount >= duplicateThreshold;
    }
}
