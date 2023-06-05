package com.ice.hxy.service.impl.post;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.ImageMapper;
import com.ice.hxy.mode.entity.Image;
import com.ice.hxy.service.PostService.IImageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 图片 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-05-05
 */
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements IImageService {

}
