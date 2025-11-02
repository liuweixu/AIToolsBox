package org.example.chatbox.box.multimodal.controller;

import jakarta.annotation.Resource;
import org.example.chatbox.box.multimodal.imagemodel.Text2ImageService;
import org.example.chatbox.common.BaseResponse;
import org.example.chatbox.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("multimodal/text2image")
public class Text2ImageController {

    @Resource
    private Text2ImageService text2ImageService;

    @GetMapping("/")
    public BaseResponse<String> getImageUrl(
            @RequestParam String prompt,
            @RequestParam String size,
            @RequestParam int number) {
        return ResultUtils.success(text2ImageService.getImageUrl(prompt, size, number));
    }
}
