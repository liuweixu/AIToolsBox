package org.example.chatbox.controller;

import com.alibaba.dashscope.utils.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.chatbox.box.unity.app.UnityApp;
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
@RequestMapping("/unity")
@Slf4j
public class UnityController {

    @Resource
    private UnityApp unityApp;

    @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatWithUnityAppSSE(
            @RequestParam String message,
            @RequestParam String chatId,
            @RequestParam String modelName) {
        log.info("modelName: {}", modelName);
        return unityApp.doChatByStream(message, chatId, modelName)
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
