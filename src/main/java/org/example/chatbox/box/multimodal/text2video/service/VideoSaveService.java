package org.example.chatbox.box.multimodal.text2video.service;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.mybatisflex.core.service.IService;
import org.example.chatbox.box.multimodal.text2image.entity.ImageSave;
import org.example.chatbox.box.multimodal.text2video.entity.VideoSave;

import java.util.List;

/**
 * 生成视频存储 服务层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
public interface VideoSaveService extends IService<VideoSave> {

    /***
     * 将视频上传到COS
     * @param videoSynthesisResult
     * @return
     */
    public String videoToSave(VideoSynthesisResult videoSynthesisResult, String prompt);

    /**
     * 获取视频所有链接
     *
     * @return
     */
    public List<VideoSave> videoSaveList();

    /**
     * 删除视频
     *
     * @param id
     * @return
     */
    public boolean deleteVideo(Long id);
}
