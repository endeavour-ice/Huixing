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

    B<Boolean> addPost(AddPostRequest postRequest, MultipartFile file);


    B<Boolean> doThumb(PostDoThumbRequest postDoThumbRequest);

    B<Boolean> doCollect(PostDoThumbRequest postDoThumbRequest);

    B<Boolean> doComment(AddCommentRequest commentRequest);

    B<Boolean> delPost(IdRequest idRequest);

    B<Map<String, Object>> getPostByCollection();


    B<PostVo> getPost(Long postId);

    B<List<PostVo>> getPostByCollect();

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
