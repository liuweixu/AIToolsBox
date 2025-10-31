package org.example.chatbox.controller;

import com.alibaba.dashscope.utils.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.chatbox.box.unity.app.UnityApp;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/unity")
@Slf4j
public class UnityController {

    @Resource
    private UnityApp unityApp;

    @GetMapping("/chat/sse")
    public Flux<ServerSentEvent<String>> doChatWithUnityAppSSE(
            @RequestParam String message,
            @RequestParam String chatId,
            @RequestParam String modelName) {
        log.info("modelName: {}", modelName);
        return unityApp.doChatByStream(message, chatId, modelName)
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JsonUtils.toJson(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                });
    }
}
