package org.example.chatbox.box.multimodal.controller;

import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import jakarta.annotation.Resource;
import org.example.chatbox.box.multimodal.text2video.entity.VideoSave;
import org.example.chatbox.box.multimodal.text2video.entity.VideoSaveDelete;
import org.example.chatbox.box.multimodal.text2video.service.Text2VideoService;
import org.example.chatbox.box.multimodal.text2video.service.VideoSaveService;
import org.example.chatbox.common.BaseResponse;
import org.example.chatbox.common.ResultUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/multimodal/text2video")
public class Text2VideoController {

    @Resource
    private Text2VideoService text2VideoService;

    @Resource
    private VideoSaveService videoSaveService;

    @GetMapping("/generate")
    public BaseResponse<String> getVideoUrl(
            @RequestParam String prompt,
            @RequestParam String size,
            @RequestParam int duration) {
        VideoSynthesisResult videoSynthesisResult = text2VideoService.getVideoUrl(prompt, size, duration);
        return ResultUtils.success(videoSaveService.videoToSave(videoSynthesisResult, prompt));
    }

    @GetMapping("/list")
    public BaseResponse<List<VideoSave>> videoSaveList() {
        return ResultUtils.success(videoSaveService.videoSaveList());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteVideo(@RequestBody VideoSaveDelete videoSaveDelete) {
        return ResultUtils.success(videoSaveService.deleteVideo(videoSaveDelete.getId()));
    }
}
