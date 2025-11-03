package org.example.aitoolsbox.box.multimodal.text2image.service.impl;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.box.multimodal.text2image.entity.ImageSave;
import org.example.aitoolsbox.box.multimodal.text2image.mapper.ImageSaveMapper;
import org.example.aitoolsbox.box.multimodal.text2image.service.ImageSaveService;
import org.example.aitoolsbox.common.CosManager;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * 生成图片存储 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
@Slf4j
public class ImageSaveServiceImpl extends ServiceImpl<ImageSaveMapper, ImageSave> implements ImageSaveService {

    @Resource
    private CosManager cosManager;

    @Override
    public String imageToSave(ImageSynthesisResult imageSynthesisResult) {
        try {
            String generatedUrl = imageSynthesisResult.getOutput().getResults().getFirst().getOrDefault("url", null);
            String imgUrl = uploadGeneratedImageToCOS(generatedUrl);
            String originPrompt = imageSynthesisResult.getOutput().getResults().getFirst().getOrDefault("orig_prompt", null);
            String augmentPrompt = imageSynthesisResult.getOutput().getResults().getFirst().getOrDefault("actual_prompt", null);
            ImageSave imageSave = new ImageSave();
            imageSave.setImgUrl(imgUrl);
            imageSave.setMessage(originPrompt);
            imageSave.setMessageAugment(augmentPrompt);
            log.info("generatedUrl: {}", generatedUrl);
            log.info("imgUrl: {}", imgUrl);
            this.save(imageSave);
            return imgUrl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取所有图片链接
     *
     * @return
     */
    @Override
    public List<ImageSave> imageSaveList() {
        return this.getMapper().selectAll();
    }

    /**
     * 删除图片
     *
     * @param id
     * @return
     */
    @Override
    public boolean deleteImage(Long id) {
        return this.removeById(id);
    }

    /**
     * 将生成图片的链接处理，作为一个File类上传到COS中
     *
     * @param generatedUrl
     * @return
     */
    private String uploadGeneratedImageToCOS(String generatedUrl) throws IOException {
        File tempFile = null;
        try {
            URL url = URI.create(generatedUrl).toURL();
            InputStream in = url.openStream();

            // 创建临时文件（使用UUID确保唯一性，添加.jpg后缀）
            String uuid = UUID.randomUUID().toString();
            String tempFileName = "temp_image_" + uuid + ".png";
            tempFile = File.createTempFile(tempFileName.substring(0, tempFileName.lastIndexOf(".")),
                    tempFileName.substring(tempFileName.lastIndexOf(".")));

            // 将InputStream复制到临时文件
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            // 2. 生成COS对象键（可以根据需要自定义路径格式）
            String cosKey = "/images/" + uuid + ".png";
            String cosUrl = cosManager.uploadFile(cosKey, tempFile);
            return cosUrl;
        } finally {
            // 4. 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    // 可以考虑添加到删除队列或使用定时任务清理
                    tempFile.deleteOnExit();
                }
            }
        }
    }
}
