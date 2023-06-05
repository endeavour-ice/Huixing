package com.ice.hxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.hxy.mode.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 评论表 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2023-02-15
 */
@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {

    Integer removeListByPostId(@Param("postIds") List<String> postIds);
}
