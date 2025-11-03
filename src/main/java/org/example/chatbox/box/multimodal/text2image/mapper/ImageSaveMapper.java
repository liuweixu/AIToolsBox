package org.example.chatbox.box.multimodal.text2image.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.chatbox.box.multimodal.text2image.entity.ImageSave;

/**
 * 生成图片存储 映射层。
 *
 * @author <a href="https://github.com/liuweixu">liuweixu</a>
 */
@Mapper
public interface ImageSaveMapper extends BaseMapper<ImageSave> {

}
