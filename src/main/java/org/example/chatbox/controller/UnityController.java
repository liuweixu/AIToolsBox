package org.example.chatbox.controller;

import jakarta.annotation.Resource;
import org.example.chatbox.box.unity.app.UnityApp;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/unity")
public class UnityController {

    @Resource
    private UnityApp unityApp;
    
    @GetMapping("/chat/sse")
    public Flux<ServerSentEvent<String>> doChatWithUnityAppSSE(String message, String chatId, String modelName) {
        return unityApp.doChatByStream(message, chatId, modelName)
                .map(chunk -> ServerSentEvent
                        .<String>builder()
                        .data(chunk)
                        .build());
    }
}
