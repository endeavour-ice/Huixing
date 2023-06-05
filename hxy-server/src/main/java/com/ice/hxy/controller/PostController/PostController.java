package com.ice.hxy.controller.PostController;

import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.dto.PageRequest;
import com.ice.hxy.mode.entity.vo.PostVo;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.*;
import com.ice.hxy.mode.resp.PageResp;
import com.ice.hxy.mode.resp.PostListPageResponse;
import com.ice.hxy.service.PostService.IPostService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 帖子 前端控制器
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
@RestController
@RequestMapping("/post")
public class PostController {
    @Resource
    private IPostService postService;

    @PostMapping("/addPost")
    public B<Boolean> addPost(@RequestBody AddPostRequest postRequest, MultipartFile file) {
        return postService.addPost(postRequest, file);

    }

    @PostMapping("/index")
    @CurrentLimiting
    public B<PageResp<List<PostVo>>> getPostListByIndex(@RequestBody PostPageReq postPageReq) {
        return postService.getPostListByIndex(postPageReq);
    }
    @PostMapping("/user")
    @CurrentLimiting
    public B<PageResp<List<PostVo>>> getPostListByUser(@RequestBody PostTeamPageReq postTeamPageReq) {
        return postService.getPostListByUser(postTeamPageReq);
    }
    @PostMapping("team/list")
    public B<PostListPageResponse> getPostTeamList(@RequestBody PostPageRequest postPageRequest) {
        return postService.getPostTeamList(postPageRequest);
    }

    @PostMapping("/team")
    @CurrentLimiting
    public B<PageResp<List<PostVo>>> getPostTeamList(@RequestBody PostTeamPageReq teamPageReq) {
        return postService.getPostTeamList(teamPageReq);
    }

    @GetMapping("/get")
    public B<PostVo> getPostById(@RequestParam Long postId) {
        return postService.getPost(postId);
    }

    @GetMapping("/col")
    public B<Map<String, Object>> getPostByCollection() {
        return postService.getPostByCollection();
    }


    @PostMapping("/doThumb")
    @CurrentLimiting
    public B<Boolean> doThumb(@RequestBody PostDoThumbRequest postDoThumbRequest) {
        return postService.doThumb(postDoThumbRequest);
    }


    @PostMapping("/doCollect")
    @CurrentLimiting
    public B<Boolean> doCollect(@RequestBody PostDoThumbRequest postDoThumbRequest) {
        return postService.doCollect(postDoThumbRequest);
    }

    @PostMapping("/doComment")
    public B<Boolean> doComment(@RequestBody AddCommentRequest commentRequest) {
        return postService.doComment(commentRequest);
    }

    @PostMapping("/delPost")
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<Boolean> delPost(@RequestBody IdRequest idRequest) {
        return postService.delPost(idRequest);
    }

    @GetMapping("/collect")
    public B<List<PostVo>> getPostByCollect() {
        return postService.getPostByCollect();
    }

    @PostMapping("/record")
    public B<Map<String, Object>> getPostByRecord(@RequestBody PageRequest pageRequest) {
        return postService.getPostByRecord(pageRequest);
    }

    @PostMapping("/search")
    public B<List<PostVo>> searchPost(@RequestBody SearchPostRequest searchPostRequest) {
        return postService.searchPost(searchPostRequest);
    }


    @PostMapping("/del")
    public B<Boolean> delComment(@RequestBody DelCommRequest commRequest) {
        return postService.delComment(commRequest);
    }

    @PostMapping("/image")
    @CurrentLimiting
    public B<Map<String, Object>> images(@RequestBody PageRequest pageRequest) {
        return postService.imagePage(pageRequest);
    }


    @PostMapping("/delete/team")
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<Boolean> deleteTeamById(@RequestBody Long teamId) {
        return postService.deleteTeamById(teamId);
    }
}
