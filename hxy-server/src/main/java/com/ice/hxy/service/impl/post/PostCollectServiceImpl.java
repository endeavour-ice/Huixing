package com.ice.hxy.service.impl.post;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.PostCollectMapper;
import com.ice.hxy.mode.entity.PostCollect;
import com.ice.hxy.service.PostService.IPostCollectService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 帖子收藏记录 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-03-10
 */
@Service
public class PostCollectServiceImpl extends ServiceImpl<PostCollectMapper, PostCollect> implements IPostCollectService {

    @Override
    public boolean removeListByPostId(List<String> postIds) {
        Integer integer = baseMapper.removeListByPostId(postIds);
        if (integer == null) {
            return false;
        }
        return integer >= 0;
    }
}
