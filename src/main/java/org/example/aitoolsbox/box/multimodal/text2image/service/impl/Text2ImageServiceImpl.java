package org.example.aitoolsbox.box.multimodal.text2image.service.impl;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.box.multimodal.text2image.service.Text2ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class Text2ImageServiceImpl implements Text2ImageService {

    @Value("${alibaba.api_key}")
    private String apiKey;
    @Value("${alibaba.base_url}")
    private String baseImageUrl;
    @Value("${alibaba.image.model_name}")
    private String modelName;

    public ImageSynthesisResult getImageUrl(String prompt, String size, int number) {
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
                        .n(number)
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
        log.info("imgUrl:{}", result.getOutput().getResults().getFirst().getOrDefault("url", null));
        return result;
    }
}
