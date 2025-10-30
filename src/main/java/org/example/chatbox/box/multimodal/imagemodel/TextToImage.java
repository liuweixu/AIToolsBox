package org.example.chatbox.box.multimodal.imagemodel;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TextToImage {

    @Value("${alibaba.image.model.api_key}")
    private String apiKey;
    @Value("${alibaba.image.model.base_url}")
    private String baseImageUrl;
    @Value("${alibaba.image.model.model_name}")
    private String modelName;

    public String getImageUrl(String prompt, String size, String number) {
        // 设置parameters参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("prompt_extend", true);
        parameters.put("watermark", false);
        parameters.put("seed", 12345);

        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .baseImageUrl(baseImageUrl)
                        .apiKey(apiKey)
                        .model(modelName)
                        .prompt(prompt)
                        .n(Integer.valueOf(number))
                        .size(size)
                        .negativePrompt("")
                        .parameters(parameters)
                        .build();

        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            System.out.println("---sync call, please wait a moment----");
            result = imageSynthesis.call(param);
        } catch (ApiException | NoApiKeyException e) {
            throw new RuntimeException(e.getMessage());
        }
        return result.getOutput().getResults().getFirst().getOrDefault("url", "");
    }
}
