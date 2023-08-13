package com.ice.hxy.service.PostService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.dto.PageRequest;
import com.ice.hxy.mode.entity.Post;
import com.ice.hxy.mode.entity.vo.PostVo;
import com.ice.hxy.mode.request.*;
import com.ice.hxy.mode.resp.PageResp;
import com.ice.hxy.mode.resp.PostListPageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 帖子 服务类
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
public interface IPostService extends IService<Post> {
    /**
     * 添加文章
     * @param postRequest
     * @param file
     * @return
     */
    B<Boolean> addPost(AddPostRequest postRequest, MultipartFile file);

    /**
     * 点赞
     * @param postDoThumbRequest
     * @return
     */
    B<Boolean> doThumb(PostDoThumbRequest postDoThumbRequest);

    /**
     * 收藏
     * @param postDoThumbRequest
     * @return
     */
    B<Boolean> doCollect(PostDoThumbRequest postDoThumbRequest);

    /**
     * 评论
     * @param commentRequest
     * @return
     */
    B<Boolean> doComment(AddCommentRequest commentRequest);

    /**
     * 删除文章
     * @param idRequest
     * @return
     */
    B<Boolean> delPost(IdRequest idRequest);

    /**
     * 获取获取收藏数
     * @return
     */
    B<Map<String, Object>> getPostByCollection();

    /**
     * 更具id获取文章
     * @param postId
     * @return
     */
    B<PostVo> findPostById(Long postId);

    /**
     * 根据收藏获取文章
     * @return
     */
    B<List<PostVo>> findPostByCollect();

    B<List<PostVo>> searchPost(SearchPostRequest searchPostRequest);

    /**
     * 删除评论
     *
     * @param idRequest
     * @return
     */
    B<Boolean> delComment(DelCommRequest idRequest);

    B<Map<String, Object>> imagePage(PageRequest pageRequest);

    B<Map<String, Object>> getPostByRecord(PageRequest pageRequest);

    B<PostListPageResponse> getPostTeamList(PostPageRequest postPageRequest);


    B<Boolean> deleteTeamById(Long teamId);

    B<PageResp<List<PostVo>>> getPostListByIndex(PostPageReq postPageReq);

    B<PageResp<List<PostVo>>> getPostTeamList(PostTeamPageReq teamPageReq);

    B<PageResp<List<PostVo>>> getPostListByUser(PostTeamPageReq postTeamPageReq);
}
