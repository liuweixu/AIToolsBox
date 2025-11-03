package org.example.chatbox.box.multimodal.text2video.service;

import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;

public interface Text2VideoService {

    /**
     * 生成视频
     *
     * @param prompt
     * @param size
     * @param number
     * @return
     */
    public VideoSynthesisResult getVideoUrl(String prompt, String size, int number);
}
