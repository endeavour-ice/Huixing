package com.ice.hxy.service.PostService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.mode.entity.PostComment;

import java.util.List;

/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author ice
 * @since 2023-02-15
 */
public interface IPostCommentService extends IService<PostComment> {


    boolean removeListByPostId(List<String> postIds);
}
