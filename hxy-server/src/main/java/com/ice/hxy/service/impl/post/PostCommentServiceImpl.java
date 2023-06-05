package com.ice.hxy.service.impl.post;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.mapper.PostCommentMapper;
import com.ice.hxy.mode.entity.PostComment;
import com.ice.hxy.service.PostService.IPostCommentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-02-15
 */
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements IPostCommentService {


    @Override
    public boolean removeListByPostId(List<String> postIds) {
        Integer integer = baseMapper.removeListByPostId(postIds);
        if (integer == null) {
            return false;
        }
        return integer >= 0;
    }
}
