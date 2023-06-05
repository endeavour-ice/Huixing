package com.ice.hxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ice.hxy.mode.entity.Image;
import com.ice.hxy.mode.entity.Post;
import com.ice.hxy.mode.entity.vo.CollectThumbVo;
import com.ice.hxy.mode.entity.vo.CommentVo;
import com.ice.hxy.mode.entity.vo.PostUserVo;
import com.ice.hxy.mode.entity.vo.PostVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 帖子 Mapper 接口
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
    List<CommentVo> getPostCommentByPostIds(@Param("postIds") Collection<Long> postIds);

    List<CommentVo> getPostCommentByPostId(@Param("postId") long postId);

    List<PostVo> selectPostByUserOrderPage(@Param("pageNum") long pageNum,
                                           @Param("pageSize") long pageSize,
                                           @Param("sorted") int sorted,
                                           @Param("userId") Long userId,
                                           @Param("groupId") Long groupId);

    List<PostVo> selectPostByUserOrderPageNotInId(@Param("pageNum") long pageNum,
                                                  @Param("pageSize") long pageSize,
                                                  @Param("sorted") int sorted,
                                                  @Param("ids") Collection<?> ids);

    List<PostUserVo> selectPostThumbTotal(@Param("userIdList") Collection<Long> userIdList);

    int selectCountById(@Param("groupId") Long groupId);

    List<CollectThumbVo> selectCTByPostIds(@Param("postIds") Collection<?> postIds, @Param("userId") long userId);

    PostVo selectPostUserOrderById(@Param("id") long postId);

    List<PostVo> selectPostCollectByUserId(@Param("userId") long userId);

    List<PostVo> searchContent(@Param("content") String content);

    List<PostVo> searchUser(@Param("userId") long userId);

    List<PostVo> selectPostVoByIds(@Param("ids") Collection<?> ids);

    List<Image> selectPageByImageDesc(@Param("current") long current, @Param("size") long size);

    long selectCountByGroupIdUserId(@Param("groupId") Long groupId,@Param("userId")Long userId);

    List<String> selectPostIdsByGroupId(@Param("id") long teamId);

    List<PostVo> getPostPageByDESC(@Param("groupId") Long groupId,@Param("userId") Long userId, @Param("current") long current, @Param("size") long size);

    List<PostVo> getPostPageByTHUMB(@Param("groupId") Long groupId,@Param("userId") Long userId, @Param("current") long current, @Param("size") long size);

    List<PostVo> getPostPageByCOMMENT(@Param("groupId") Long groupId,@Param("userId") Long userId, @Param("current") long current, @Param("size") long size);

    List<PostVo> getPostPageByHot(@Param("groupId") Long groupId,@Param("userId") Long userId, @Param("current") long current, @Param("size") long size);
}