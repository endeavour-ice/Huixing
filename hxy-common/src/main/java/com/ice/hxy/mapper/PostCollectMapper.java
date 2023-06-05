package com.ice.hxy.mapper;

import com.ice.hxy.mode.entity.PostCollect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 帖子收藏记录 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2023-03-10
 */
@Mapper
public interface PostCollectMapper extends BaseMapper<PostCollect> {

    Integer removeListByPostId(@Param("postIds") List<String> postIds);
}
