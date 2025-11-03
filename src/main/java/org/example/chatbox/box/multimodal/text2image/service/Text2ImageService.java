package org.example.chatbox.box.multimodal.text2image.service;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;

public interface Text2ImageService {
    /**
     * 文字生成图片
     *
     * @param prompt
     * @param size
     * @param number
     * @return
     */
    public ImageSynthesisResult getImageUrl(String prompt, String size, int number);
}
