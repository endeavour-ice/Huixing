package com.ice.hxy.service.PostService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.mode.entity.PostCollect;

import java.util.List;

/**
 * <p>
 * 帖子收藏记录 服务类
 * </p>
 *
 * @author ice
 * @since 2023-03-10
 */
public interface IPostCollectService extends IService<PostCollect> {

    boolean removeListByPostId(List<String> postIds);
}
