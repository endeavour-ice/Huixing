package com.ice.hxy.mq.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.mode.mq.MqClient;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.Post;
import com.ice.hxy.mode.entity.PostRead;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.service.commService.RedisCache;

import com.ice.hxy.service.PostService.IPostCollectService;
import com.ice.hxy.service.PostService.IPostReadService;
import com.ice.hxy.service.PostService.IPostService;
import com.ice.hxy.service.PostService.IPostThumbService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.util.AlgorithmUtils;
import com.ice.hxy.util.LongUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/4/30 16:56
 * @Description: 文章监听器
 */
@Component
@Slf4j
public class PostListener {
    @Resource
    private SaveMessageMq saveMessageMq;
    @Resource
    private IPostService postService;
    @Autowired
    private IPostReadService readService;
    @Autowired
    private IUserService userService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IPostThumbService thumbService;
    @Autowired
    private IPostCollectService collectService;

    @RabbitListener(queues = MqClient.READ_POST_QUEUE)
    public void removeRedisByKey(Message message, Channel channel, String json) {
        boolean isMess = saveMessageMq.saveMessage(message);
        if (!isMess && !StringUtils.hasText(json)) {
            return;
        }
        Gson gson = GsonUtils.getGson();
        Map<String, Long> map = gson.fromJson(json, new TypeToken<Map<String, Long>>() {
        }.getType());
        Long userId = map.get("userId");
        Long postId = map.get("postId");
        if (!LongUtil.isEmpty(postId)) {
            postService.update()
                    .eq("id", postId)
                    .setSql("view_num=view_num+1").update();
        }
        if (LongUtil.isEmpty(userId) || LongUtil.isEmpty(postId)) {
            return;
        }

        try {
            match(userId, postId);
        } catch (Exception ignored) {

        }

        QueryWrapper<PostRead> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("post_id", postId);
        long count = readService.count(wrapper);
        if (count > 0) {
            readService.update().setSql("read_count=read_count+1").update();
        } else {
            PostRead postRead = new PostRead();
            postRead.setPostId(postId);
            postRead.setUserId(userId);
            readService.save(postRead);
        }

    }

    public void matchPost(Long userId, Long postId) {
        // 这里操作的数据量大 可以使用定时任务
        // 获取用户所有的点赞,收藏,浏览的文章id

        // 根据文章的id进行对该文章被收藏，点赞，浏览的用户进行余弦向量度的计算
        // 获取所有的用户点赞(其次)，收藏(优先)，浏览（最后)的文章保存到
    }

    public void match(Long userId, Long postId) {
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            return;
        }
        Long loginUserId = loginUser.getId();
        Post post = postService.getById(postId);
        QueryWrapper<PostRead> wrapper = new QueryWrapper<>();
        wrapper.select("user_id");
        wrapper.eq("post_id", postId);
        List<PostRead> readList = readService.list(wrapper);
        if (CollectionUtils.isEmpty(readList)) {
            return;
        }


        List<Long> userIds = readList.stream().map(PostRead::getUserId).collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIds);
        if (CollectionUtils.isEmpty(userList)) {
            return;
        }

        wrapper = new QueryWrapper<>();
        wrapper.select("post_id", "user_id","read_count");
        wrapper.in("user_id", userIds);
        List<PostRead> postReads = readService.list(wrapper);
        if (CollectionUtils.isEmpty(postReads)) {
            return;
        }
        List<Long> postIds = postReads.stream().map(PostRead::getPostId).collect(Collectors.toList());
        List<Post> postList = postService.listByIds(postIds);
        if (CollectionUtils.isEmpty(postList)) {
            return;
        }
        Map<Long, List<Post>> postMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
        Gson gson = GsonUtils.getGson();
        String loginUserTags = loginUser.getTags();
        List<String> loginUserTagList = null;
        if (StringUtils.hasText(loginUserTags)) {
            loginUserTagList = gson.fromJson(loginUserTags, new TypeToken<List<String>>() {
            }.getType());
        }
        Map<Long, Integer> map = new HashMap<>();
        for (User user : userList) {
            int distance = 0;
            if (StringUtils.hasText(loginUserTags) && loginUserTagList != null) {
                if (user == null || userId.equals(user.getId())) {
                    continue;
                }
                String tags = user.getTags();
                if (!StringUtils.hasText(tags)) {
                    continue;
                }
                List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
                }.getType());
                distance = AlgorithmUtils.minDistance(loginUserTagList, tagList);
                if (StringUtils.hasText(loginUser.getProfile())) {
                    double similarity = AlgorithmUtils.getSimilarity(loginUser.getProfile(), user.getProfile());
                    distance += similarity;
                }
            }
            map.put(user.getId(), distance);

        }
        Set<DefaultTypedTuple<Long>> set = new HashSet<>();

        for (PostRead postRead : postReads) {
            Long readUserId = postRead.getUserId();
            if (readUserId.equals(userId)) {
                continue;
            }
            double distance = map.get(readUserId);
            Long readCount = postRead.getReadCount();
            if (readCount > 0) {
                distance += (readCount.doubleValue() / 10);
            }
            Post p = postMap.get(postRead.getPostId()).get(0);
            String content = post.getContent();
            String con = p.getContent();
            double similarity = AlgorithmUtils.getSimilarity(content, con);
            distance += similarity;
            DefaultTypedTuple<Long> tuple = new DefaultTypedTuple<>(postRead.getPostId(), distance);
            set.add(tuple);
        }
        if (set.size() > 0) {
            redisCache.addZSet(CacheConstants.MATCH_POST + loginUserId, set);
        }

    }

}
