package org.example.chatbox.box.multimodal.controller;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import jakarta.annotation.Resource;
import org.example.chatbox.box.multimodal.text2image.entity.ImageSave;
import org.example.chatbox.box.multimodal.text2image.entity.ImageSaveDelete;
import org.example.chatbox.box.multimodal.text2image.service.ImageSaveService;
import org.example.chatbox.box.multimodal.text2image.service.Text2ImageService;
import org.example.chatbox.common.BaseResponse;
import org.example.chatbox.common.ResultUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("multimodal/text2image")
public class Text2ImageController {

    @Resource
    private Text2ImageService text2ImageService;

    @Resource
    private ImageSaveService imageSaveService;


    @GetMapping("/generate")
    public BaseResponse<String> getImageUrl(
            @RequestParam String prompt,
            @RequestParam String size,
            @RequestParam int number) {
        ImageSynthesisResult generatedUrl = text2ImageService.getImageUrl(prompt, size, number);
        return ResultUtils.success(imageSaveService.imageToSave(generatedUrl));
    }

    @GetMapping("/list")
    public BaseResponse<List<ImageSave>> imageSaveList() {
        return ResultUtils.success(imageSaveService.imageSaveList());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteImage(@RequestBody ImageSaveDelete imageSaveDelete) {
        return ResultUtils.success(imageSaveService.deleteImage(imageSaveDelete.getId()));
    }
}
