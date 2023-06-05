package com.ice.hxy.service.impl.post;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.PostThumbMapper;
import com.ice.hxy.mode.entity.PostThumb;
import com.ice.hxy.service.PostService.IPostThumbService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 帖子点赞记录 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-02-14
 */
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb> implements IPostThumbService {

    /**
     * 根据postId删除
     *
     * @param ids
     * @return
     */
    @Override
    public boolean removeListByPostId(List<String> ids) {
        Integer integer = baseMapper.removeListByPostId(ids);
        if (integer == null) {
            return false;
        }
        return integer >= 0;
    }
}
