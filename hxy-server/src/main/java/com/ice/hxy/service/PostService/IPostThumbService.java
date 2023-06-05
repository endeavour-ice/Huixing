package com.ice.hxy.service.PostService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.mode.entity.PostThumb;

import java.util.List;

/**
 * <p>
 * 帖子点赞记录 服务类
 * </p>
 *
 * @author ice
 * @since 2023-02-14
 */
public interface IPostThumbService extends IService<PostThumb> {


    boolean removeListByPostId(List<String> ids);
}
