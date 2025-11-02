package org.example.chatbox.box.multimodal.imagemodel;

import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Text2VideoService {

    @Value("${alibaba.api_key}")
    private String apiKey;
    @Value("${alibaba.base_url}")
    private String baseImageUrl;
    @Value("${alibaba.video.model_name}")
    private String modelName;


    public String getVideoUrl(String prompt, String size, int number) {
        Constants.baseHttpApiUrl = baseImageUrl;
        // 设置parameters参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("prompt_extend", true);
        parameters.put("watermark", false);
        parameters.put("seed", 12345);

        VideoSynthesisParam param =
                VideoSynthesisParam.builder()
                        .apiKey(apiKey)
                        .model(modelName)
                        .prompt(prompt)
                        .duration(number)
                        .size(size)
                        .negativePrompt("")
                        .parameters(parameters)
                        .build();

        VideoSynthesis videoSynthesis = new VideoSynthesis();
        VideoSynthesisResult result = null;
        try {
            System.out.println("---sync call, please wait a moment----");
            result = videoSynthesis.call(param);
        } catch (InputRequiredException | NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        return result.getOutput().getVideoUrl();
    }
}
