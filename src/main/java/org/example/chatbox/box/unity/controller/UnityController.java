package org.example.chatbox.box.unity.controller;

import com.alibaba.dashscope.utils.JsonUtils;
import com.networknt.schema.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.chatbox.box.unity.app.UnityApp;
import org.example.chatbox.box.unity.chat_history.service.ChatHistoryService;
import org.example.chatbox.box.unity.enums.ChatHistoryMessageTypeEnum;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/unity/model")
@Slf4j
public class UnityController {

    @Resource
    private UnityApp unityApp;

    @Resource
    private ChatHistoryService chatHistoryService;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatWithUnityAppSSE(
            @RequestParam String message,
            @RequestParam String chatId,
            @RequestParam String modelName) {
        log.info("modelName: {}", modelName);
        log.info("chatId: {}", chatId);
        // 添加用户信息到历史信息
        chatHistoryService.addChatHistory(Long.valueOf(chatId), message, ChatHistoryMessageTypeEnum.USER.getValue());
        Flux<String> contentFlux = unityApp.doChatByStream(message, chatId, modelName);
        // 收集AI响应内容，并在完成后记录到对话历史
        StringBuilder stringBuilder = new StringBuilder();
        return contentFlux
                .map(chunk -> {
                    stringBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    String aiResponse = stringBuilder.toString();
                    if (!StringUtils.isBlank(aiResponse)) {
                        chatHistoryService.addChatHistory(Long.valueOf(chatId), aiResponse,
                                ChatHistoryMessageTypeEnum.AI.getValue());
                    }
                })
                .doOnError(throwable -> {
                    // 如果回复失败，也要记录失败信息
                    String errorResponse = "AI回复失败" + throwable.getMessage();
                    chatHistoryService.addChatHistory(Long.valueOf(chatId), errorResponse,
                            ChatHistoryMessageTypeEnum.AI.getValue());
                })
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("datastream", chunk);
                    String jsonData = JsonUtils.toJson(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        // 发送结束事件，因为只有一个事件，故而用Mono
                        ServerSentEvent.<String>builder()
                                .event("done") // 在返回的流式数据中，会出现“event:done”，到时候前端判断就行
                                .data("")
                                .build()
                ));
    }
}
