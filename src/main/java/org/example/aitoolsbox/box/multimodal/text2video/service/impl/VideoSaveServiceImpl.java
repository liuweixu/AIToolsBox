package org.example.aitoolsbox.box.multimodal.text2video.service.impl;

import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aitoolsbox.box.multimodal.text2video.entity.VideoSave;
import org.example.aitoolsbox.box.multimodal.text2video.mapper.VideoSaveMapper;
import org.example.aitoolsbox.box.multimodal.text2video.service.VideoSaveService;
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
 * 生成视频存储 服务层实现。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Service
@Slf4j
public class VideoSaveServiceImpl extends ServiceImpl<VideoSaveMapper, VideoSave> implements VideoSaveService {

    @Resource
    private CosManager cosManager;

    /***
     * 将视频上传到COS
     * @param videoSynthesisResult
     * @return
     */
    @Override
    public String videoToSave(VideoSynthesisResult videoSynthesisResult, String prompt) {
        try {
            String generatedUrl = videoSynthesisResult.getOutput().getVideoUrl();
            String videoUrl = uploadGeneratedVideoToCOS(generatedUrl);
            String originPrompt = prompt;
            String augmentPrompt = prompt;
            VideoSave videoSave = new VideoSave();
            videoSave.setVideoUrl(videoUrl);
            videoSave.setMessage(originPrompt);
            videoSave.setMessageAugment(augmentPrompt);
            log.info("generatedUrl: {}", generatedUrl);
            log.info("videoUrl: {}", videoUrl);
            this.save(videoSave);
            return videoUrl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取视频所有链接
     *
     * @return
     */
    @Override
    public List<VideoSave> videoSaveList() {
        return this.getMapper().selectAll();
    }

    /**
     * 删除视频
     *
     * @param id
     * @return
     */
    @Override
    public boolean deleteVideo(Long id) {
        return this.removeById(id);
    }


    /**
     * 将生成视频的链接处理，作为一个File类上传到COS中
     *
     * @param generatedUrl
     * @return
     */
    private String uploadGeneratedVideoToCOS(String generatedUrl) throws IOException {
        File tempFile = null;
        try {
            URL url = URI.create(generatedUrl).toURL();
            InputStream in = url.openStream();

            // 创建临时文件（使用UUID确保唯一性，添加.jpg后缀）
            String uuid = UUID.randomUUID().toString();
            String tempFileName = "temp_video_" + uuid + ".mp4";
            tempFile = File.createTempFile(tempFileName.substring(0, tempFileName.lastIndexOf(".")),
                    tempFileName.substring(tempFileName.lastIndexOf(".")));

            // 将InputStream复制到临时文件
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            // 2. 生成COS对象键（可以根据需要自定义路径格式）
            String cosKey = "/videos/" + uuid + ".mp4";
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
