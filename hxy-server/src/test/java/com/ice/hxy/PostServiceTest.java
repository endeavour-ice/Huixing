package com.ice.hxy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.hxy.mode.entity.Post;
import com.ice.hxy.mode.entity.PostGroup;
import com.ice.hxy.service.PostService.IPostService;
import com.ice.hxy.service.PostService.PostGroupService;
import com.ice.hxy.util.Threads;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/5/27 20:37
 * @Description: TODO
 */
@SpringBootTest
public class PostServiceTest {
    @Autowired
    private IPostService postService;

    @Resource
    private PostGroupService postGroupService;

    @Test
    void transferPost() {
        List<Post> list = postService.lambdaQuery().like(Post::getTags,"寻金之路").list();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ArrayList<PostGroup> arrayList = new ArrayList<>(6001);
        for (Post post : list) {
            PostGroup postGroup = new PostGroup();
            Long id = post.getId();
            LocalDateTime createTime = post.getCreateTime();
            LocalDateTime updateTime = post.getUpdateTime();
            postGroup.setPostId(id);
            postGroup.setGroupId(1L);
            postGroup.setCreateTime(createTime);
            postGroup.setUpdateTime(updateTime);
            arrayList.add(postGroup);
            if (arrayList.size() >= 6000) {
                ArrayList<PostGroup> finalArrayList = arrayList;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    postGroupService.saveBatch(finalArrayList);
                });
                arrayList = new ArrayList<>(6001);
                futures.add(future);
            }
        }
        if (arrayList.size() > 0) {
            postGroupService.saveBatch(arrayList);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
    }



    @Test
    void updatePost() {
        List<Post> list = postService.list();
        List<Long> ids = new ArrayList<>(6001);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Post post : list) {
            String content = post.getContent();
            if (!StringUtils.hasText(content)) {
                Long postId = post.getId();
                ids.add(postId);
            }
            if (ids.size() >= 6000) {
                List<Long> finalArrayList = ids;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    postService.removeBatchByIds(finalArrayList);
                    postGroupService.lambdaUpdate().in(PostGroup::getPostId, finalArrayList).remove();
                });
                ids = new ArrayList<>(6001);
                futures.add(future);
            }

        }
        if (ids.size() > 0) {
            postService.removeBatchByIds(ids);
            postGroupService.lambdaUpdate().in(PostGroup::getPostId, ids).remove();
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
    }

    @Test
    void upPost() {
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        LocalDateTime parse = LocalDateTime.parse("2023-05-28T00:12:25");
        LocalDateTime lt = LocalDateTime.parse("2023-05-30T00:03:48");
        postQueryWrapper.gt("create_time", parse);
        postQueryWrapper.lt("create_time", lt);
        postQueryWrapper.orderByDesc("create_time");
        List<Post> list = postService.list(postQueryWrapper);
        ArrayList<Post> arrayList = new ArrayList<>(21);
        for (Post post : list) {
            if (arrayList.size() >= 20) {
                postService.updateBatchById(arrayList);
                arrayList = new ArrayList<>(21);
                Threads.sleep(1);
            }
            LocalDateTime now = LocalDateTime.now();
            post.setCreateTime(now);
            post.setUpdateTime(now);
            arrayList.add(post);
        }
        if (arrayList.size() > 0) {
            postService.updateBatchById(arrayList);
        }

    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    void GroupPost() {
        // 2023-05-28 00:13:25
        List<Post> list = postService.list();
        LocalDateTime parse = LocalDateTime.parse("2023-05-28T00:12:25");
        ArrayList<Post> APost = new ArrayList<>();
        ArrayList<Post> XPost = new ArrayList<>();
        for (Post post : list) {
            if (post.getCreateTime().isAfter(parse)) {
                APost.add(post);
            } else {
                XPost.add(post);
            }
        }

        List<PostGroup> XP = XPost.stream().map(p -> {
            PostGroup postGroup = new PostGroup();
            Long id = p.getId();
            LocalDateTime createTime = p.getCreateTime();
            LocalDateTime updateTime = p.getUpdateTime();
            postGroup.setPostId(id);
            postGroup.setGroupId(0L);
            postGroup.setCreateTime(createTime);
            postGroup.setUpdateTime(updateTime);
            return postGroup;
        }).collect(Collectors.toList());

        List<Post> collect = APost.stream().sorted(Comparator.comparing(Post::getCreateTime, Comparator.reverseOrder())).collect(Collectors.toList());
        for (Post post : collect) {
            LocalDateTime time = LocalDateTime.now();
            PostGroup postGroup = new PostGroup();
            postGroup.setGroupId(0L);
            Long id = post.getId();
            postGroup.setPostId(id);
            postGroup.setCreateTime(time);
            postGroup.setUpdateTime(time);
            XP.add(postGroup);
            post.setCreateTime(time);
            post.setUpdateTime(time);
        }
        System.out.println(XP.size());
        System.out.println(collect.size());
        boolean saveBatch = postGroupService.saveBatch(XP);

        boolean updateBatchById = postService.updateBatchById(collect);
        System.out.println(saveBatch);
        System.out.println(updateBatchById);
    }


}
