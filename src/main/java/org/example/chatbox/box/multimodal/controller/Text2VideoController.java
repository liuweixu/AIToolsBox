package org.example.chatbox.box.multimodal.controller;

import jakarta.annotation.Resource;
import org.example.chatbox.box.multimodal.imagemodel.Text2VideoService;
import org.example.chatbox.common.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/multimodal/text2video")
public class Text2VideoController {

    @Resource
    private Text2VideoService text2VideoService;

    @GetMapping("/")
    public String getVideoUrl(
            @RequestParam String prompt,
            @RequestParam String size,
            @RequestParam int duration) {
        return text2VideoService.getVideoUrl(prompt, size, duration);
    }
}
