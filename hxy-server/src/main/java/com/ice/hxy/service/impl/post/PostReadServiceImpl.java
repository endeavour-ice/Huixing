package com.ice.hxy.service.impl.post;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.PostReadMapper;
import com.ice.hxy.mode.entity.PostRead;
import com.ice.hxy.service.PostService.IPostReadService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文章阅读 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-04-30
 */
@Service
public class PostReadServiceImpl extends ServiceImpl<PostReadMapper, PostRead> implements IPostReadService {

}
