package org.example.aitoolsbox.box.multimodal.text2image.service;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.mybatisflex.core.service.IService;
import org.example.aitoolsbox.box.multimodal.text2image.entity.ImageSave;

import java.util.List;

/**
 * 生成图片存储 服务层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
public interface ImageSaveService extends IService<ImageSave> {

    /***
     * 将图片上传到COS
     * @param imageSynthesisResult
     * @return
     */
    public String imageToSave(ImageSynthesisResult imageSynthesisResult);

    /**
     * 获取图片所有链接
     *
     * @return
     */
    public List<ImageSave> imageSaveList();

    /**
     * 删除图片
     *
     * @param id
     * @return
     */
    public boolean deleteImage(Long id);
}
