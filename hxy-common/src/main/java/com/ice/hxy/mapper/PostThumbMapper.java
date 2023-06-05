package com.ice.hxy.mapper;

import com.ice.hxy.mode.entity.PostThumb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 帖子点赞记录 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2023-02-14
 */
@Mapper
public interface PostThumbMapper extends BaseMapper<PostThumb> {

    Integer removeListByPostId(@Param("postIds") List<String> postIds);
}
