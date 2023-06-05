package com.ice.hxy.service.impl.post;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.annotation.RedissonLock;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mapper.PostMapper;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.dto.PageFilter;
import com.ice.hxy.mode.dto.PageRequest;
import com.ice.hxy.mode.entity.*;
import com.ice.hxy.mode.entity.vo.CommentVo;
import com.ice.hxy.mode.entity.vo.PostUserVo;
import com.ice.hxy.mode.entity.vo.PostVo;
import com.ice.hxy.mode.entity.vo.UserAvatarVo;
import com.ice.hxy.mode.enums.PostGroupEnum;
import com.ice.hxy.mode.enums.PostSortedEnum;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.*;
import com.ice.hxy.mode.resp.PageResp;
import com.ice.hxy.mode.resp.PostListPageResponse;
import com.ice.hxy.mq.RabbitService;
import com.ice.hxy.service.PostService.*;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.service.UserService.UserTeamService;
import com.ice.hxy.service.chatService.ITeamChatRecordService;
import com.ice.hxy.service.chatService.TeamService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * <p>
 * 帖子 服务实现类
 * </p>
 *
 * @author ice
 * @since 2023-02-13
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Resource
    private IPostThumbService thumbService;
    @Resource
    private TagsService tagsService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private IPostCommentService commentService;
    @Resource
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;
    @Resource
    private ExecutorService executorService;
    @Resource
    private IPostCollectService collectService;
    @Resource
    private IPostReadService readService;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private IImageService iImageService;
    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ITeamChatRecordService teamChatRecordService;
    @Resource
    private PostGroupService postGroupService;

    /**
     * 添加文章
     *
     * @param postRequest
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock
    public B<Boolean> addPost(AddPostRequest postRequest, MultipartFile file) {
        if (postRequest == null) {
            return B.parameter();
        }
        Gson gson = GsonUtils.getGson();
        User loginUser = UserUtils.getLoginUser();
        String postRequestTag = postRequest.getTags();
        Long userId = loginUser.getId();
        Long groupId = postRequest.getGroupId();
        String content = postRequest.getContent();
        if (!StringUtils.hasText(content)) {
            return B.parameter();
        }
        String sensitive = SensitiveUtils.sensitive(content);
        if (LongUtil.isEmpty(groupId)) {
            if (!teamService.isUserTeam(groupId, userId)) {
                return B.parameter("未加入该队伍");
            }
        } else {
            groupId = PostGroupEnum.INDEX.getValue();
        }
        if (StringUtils.hasText(postRequestTag)) {
            try {
                List<String> tags = gson.fromJson(postRequestTag, new TypeToken<List<String>>() {
                }.getType());
                if (CollectionUtils.isEmpty(tags)) {
                    return B.parameter("标签错误");
                }
                if (tags.size() > 5) {
                    return B.parameter("标签超出数量");
                }
                for (String tag : tags) {
                    if (SensitiveUtils.contains(tag)) {
                        return B.parameter("标签包含敏感消息");
                    }
                }
                postRequestTag = gson.toJson(tags);
            } catch (Exception e) {
                return B.parameter("标签错误");
            }
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(sensitive);
        post.setTags(postRequestTag);
        boolean save = this.save(post);
        if (!save) {
            return B.parameter();
        }
        if (LongUtil.isEmpty(post.getId())) {
            return B.parameter();
        }
        PostGroup postGroup = new PostGroup();
        postGroup.setPostId(post.getId());
        postGroup.setGroupId(groupId);
        save= postGroupService.save(postGroup);
        if (!save) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        if (StringUtils.hasText(postRequestTag)) {
            rabbitService.saveTag(userId, groupId, postRequestTag);
        }
        return B.ok();
    }

    /**
     * 获取文章的评论
     *
     * @param postVoList
     * @return
     */
    private List<PostVo> getPostVoList(List<PostVo> postVoList) {
        if (CollectionUtils.isEmpty(postVoList)) {
            return new ArrayList<>();
        }
        Map<Long, List<PostVo>> postIdMap = postVoList.stream().collect(Collectors.groupingBy(PostVo::getId));
        Set<Long> keySet = postIdMap.keySet();
        // 评论
        if (keySet.isEmpty()) {
            return new ArrayList<>();
        }
        CompletableFuture<Void> isThumb = null;
        // 是否点赞
        try {
            User loginUser = UserUtils.getLoginUser();
            isThumb = CompletableFuture.runAsync(() -> {
                Long userId = loginUser.getId();
                QueryWrapper<PostCollect> postCollectQueryWrapper = new QueryWrapper<>();
                postCollectQueryWrapper.in("post_id", keySet);
                postCollectQueryWrapper.eq("user_id", userId);
                postCollectQueryWrapper.select("post_id");
                List<PostCollect> collectList = collectService.list(postCollectQueryWrapper);
                for (PostCollect postCollect : collectList) {
                    Long collectPostId = postCollect.getPostId();
                    if (LongUtil.isEmpty(collectPostId)) {
                        continue;
                    }
                    List<PostVo> postVos = postIdMap.get(collectPostId);
                    if (CollectionUtils.isEmpty(postVos)) {
                        continue;
                    }
                    PostVo postVo = postVos.get(0);
                    if (postVo == null) {
                        continue;
                    }
                    postVo.setHasCollect(true);
                }
                QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
                thumbQueryWrapper.in("post_id", keySet);
                thumbQueryWrapper.eq("user_id", userId);
                thumbQueryWrapper.select("post_id");
                List<PostThumb> thumbList = thumbService.list(thumbQueryWrapper);
                for (PostThumb postThumb : thumbList) {
                    Long thumbPostId = postThumb.getPostId();
                    if (LongUtil.isEmpty(thumbPostId)) {
                        continue;
                    }
                    List<PostVo> postVos = postIdMap.get(thumbPostId);
                    if (CollectionUtils.isEmpty(postVos)) {
                        continue;
                    }
                    PostVo postVo = postVos.get(0);
                    if (postVo == null) {
                        continue;
                    }
                    postVo.setHasThumb(true);
                }
            }, executorService);
        } catch (Exception e) {
            //无登录不做特殊处理
        }

        Set<Long> userIdList = new HashSet<>();
        // 获取post的评论和用户
        List<CommentVo> postCommentByPostIds = baseMapper.getPostCommentByPostIds(keySet);
        for (CommentVo postCommentByPostId : postCommentByPostIds) {
            PostUserVo owner = postCommentByPostId.getOwner();
            if (owner == null) {
                continue;
            }
            Long id = owner.getId();
            if (id == null || id <= 0) {
                continue;
            }
            userIdList.add(id);
        }
        for (Long postId : keySet) {
            List<PostVo> postVos = postIdMap.get(postId);
            if (CollectionUtils.isEmpty(postVos)) {
                continue;
            }
            PostVo postVo = postVos.get(0);
            if (postVo == null) {
                continue;
            }
            PostUserVo userVo = postVo.getUserVo();
            if (userVo == null) {
                continue;
            }
            userIdList.add(userVo.getId());
        }
        Map<Long, List<PostUserVo>> postUserVoListById = new HashMap<>();
        if (userIdList.size() > 0) {
            postUserVoListById = baseMapper.selectPostThumbTotal(userIdList).stream().collect(Collectors.groupingBy(UserAvatarVo::getId));
        }
        Map<Long, List<CommentVo>> commentVoByPostIds = postCommentByPostIds.stream().collect(Collectors.groupingBy(CommentVo::getPostId));

        for (Long postVoId : keySet) {
            PostVo postVo = postIdMap.get(postVoId).get(0);
            PostUserVo postUserVo = postVo.getUserVo();
            Long id = postUserVo.getId();
            PostUserVo userVo = postUserVoListById.get(id).get(0);
            int postTotal = userVo.getPostTotal();
            String joinTime = userVo.getJoinTime();
            int thumbTotal = userVo.getThumbTotal();
            postUserVo.setPostTotal(postTotal);
            postUserVo.setJoinTime(joinTime);
            postUserVo.setThumbTotal(thumbTotal);
            List<CommentVo> commentVos = commentVoByPostIds.get(postVoId);
            if (!CollectionUtils.isEmpty(commentVos)) {
                for (CommentVo commentVo : commentVos) {
                    if (commentVo == null || commentVo.getOwner() == null) {
                        continue;
                    }
                    Long userId = commentVo.getOwner().getId();
                    if (userId == null) {
                        continue;
                    }

                    List<PostUserVo> postUserVos = postUserVoListById.get(userId);
                    if (postUserVos == null || postUserVos.isEmpty() || postUserVos.get(0) == null) {
                        continue;
                    }
                    PostUserVo vo = postUserVos.get(0);
                    PostUserVo owner = commentVo.getOwner();
                    if (vo == null || owner == null) {
                        continue;
                    }
                    postTotal = vo.getPostTotal();
                    joinTime = vo.getJoinTime();
                    thumbTotal = vo.getThumbTotal();
                    owner.setPostTotal(postTotal);
                    owner.setJoinTime(joinTime);
                    owner.setThumbTotal(thumbTotal);
                }
            }
            postVo.setCommentList(commentVos);
        }
        if (isThumb != null) {
            try {
                isThumb.join();
            } catch (Exception e) {
                log.error("getPostVoList isThumb.join() error:{}", e.getMessage());
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
            }
        }
        List<PostVo> list = new ArrayList<>();
        for (PostVo postVo : postVoList) {
            List<PostVo> postVos = postIdMap.get(postVo.getId());
            if (CollectionUtils.isEmpty(postVos)) {
                continue;
            }
            list.add(postVos.get(0));
        }
        return list;
    }


    @Override
    public B<Boolean> doThumb(PostDoThumbRequest postDoThumbRequest) {

        if (postDoThumbRequest == null || postDoThumbRequest.getPostId() == null) {
            return B.error();
        }
        long postId = postDoThumbRequest.getPostId();
        if (postId <= 0) {
            return B.error();
        }

        final User loginUser = UserUtils.getLoginUser();
        Post post = this.getById(postId);
        if (post == null) {
            return B.error();
        }
        Long userId = loginUser.getId();
        boolean result;
        synchronized (userId.toString().intern()) {
            QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
            thumbQueryWrapper.eq("post_id", postId);
            thumbQueryWrapper.eq("user_id", userId);
            long count = thumbService.count(thumbQueryWrapper);
            TransactionStatus transaction = null;
            if (count >= 1) {
                // 取消点赞
                try {
                    transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    result = thumbService.remove(thumbQueryWrapper);
                    if (!result) {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }
                    result = this.update()
                            .eq("id", postId)
                            .ge("thumb_num", 0)
                            .setSql("thumb_num=thumb_num-1").update();
                    if (!result) {
                        return B.error();
                    }
                    dataSourceTransactionManager.commit(transaction);
                    return B.ok(true);
                } catch (Exception e) {
                    if (transaction != null) {
                        dataSourceTransactionManager.rollback(transaction);
                    }
                    return B.error();
                }

            } else {

                // 点赞
                PostThumb postThumb = new PostThumb();
                postThumb.setPostId(postId);
                postThumb.setUserId(userId);
                try {
                    transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    result = thumbService.save(postThumb);
                    if (result) {
                        result = this.update()
                                .eq("id", postId)
                                .setSql("thumb_num=thumb_num+1").update();
                    } else {
                        throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
                    }
                    dataSourceTransactionManager.commit(transaction);
                    return B.ok(result);
                } catch (Exception e) {
                    if (transaction != null) {
                        dataSourceTransactionManager.rollback(transaction);
                    }
                    return B.error();
                }
            }
        }
    }

    @Override
    public B<Boolean> doCollect(PostDoThumbRequest postDoThumbRequest) {

        if (postDoThumbRequest == null || postDoThumbRequest.getPostId() == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        Long postId = postDoThumbRequest.getPostId();
        if (postId <= 0) {
            return B.error();
        }
        User loginUser = UserUtils.getLoginUser();
        Post post = this.getById(postId);
        if (post == null) {
            return B.error();
        }
        Long userId = loginUser.getId();
        boolean result;
        synchronized (userId.toString().intern()) {
            QueryWrapper<PostCollect> thumbQueryWrapper = new QueryWrapper<>();
            thumbQueryWrapper.eq("post_id", postId);
            thumbQueryWrapper.eq("user_id", userId);
            long count = collectService.count(thumbQueryWrapper);
            TransactionStatus transaction = null;
            if (count >= 1) {
                // 取消
                try {
                    transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    result = collectService.remove(thumbQueryWrapper);
                    if (result) {
                        result = this.update()
                                .eq("id", postId)
                                .ge("collect_num", 0)
                                .setSql("collect_num=collect_num-1").update();
                        if (result) {
                            dataSourceTransactionManager.commit(transaction);
                            return B.ok();
                        }
                    }
                    dataSourceTransactionManager.rollback(transaction);
                    return B.error();
                } catch (Exception e) {
                    if (transaction != null) {
                        dataSourceTransactionManager.rollback(transaction);
                    }
                    return B.error();
                }

            } else {
                // 收藏
                PostCollect postCollect = new PostCollect();
                postCollect.setPostId(postId);
                postCollect.setUserId(userId);
                try {
                    transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
                    result = collectService.save(postCollect);
                    if (result) {
                        result = this.update()
                                .eq("id", postId)
                                .setSql("collect_num=collect_num+1").update();
                        if (result) {
                            dataSourceTransactionManager.commit(transaction);
                            return B.ok();
                        }

                    }
                    dataSourceTransactionManager.rollback(transaction);
                    return B.error();
                } catch (Exception e) {
                    if (transaction != null) {
                        dataSourceTransactionManager.rollback(transaction);
                    }
                    return B.error();
                }

            }
        }
    }

    @Override
    public B<Boolean> doComment(AddCommentRequest commentRequest) {
        if (commentRequest == null) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        Long postId = commentRequest.getPostId();
        Long userId = commentRequest.getUserId();
        String content = commentRequest.getContent();
        if (postId == null) {
            return B.parameter();
        }
        if (!StringUtils.hasText(content)) {
            return B.parameter();
        }

        Long loginUserId = loginUser.getId();
        if (!loginUserId.equals(userId)) {
            return B.auth();
        }
        synchronized (loginUserId.toString().intern()) {
            try {
                content = SensitiveUtils.sensitive(content);
            } catch (Exception e) {
                log.error("处理错误 " + e.getMessage());
            }
            Post post = this.getById(postId);
            if (post == null) {
                return B.parameter();
            }
            QueryWrapper<PostComment> wrapper = new QueryWrapper<>();
            wrapper.eq("post_id", postId);
            wrapper.eq("user_id", userId);
            wrapper.eq("content", content);
            long count = commentService.count(wrapper);
            if (count > 2) {
                return B.parameter("评论重复");
            }
            PostComment postComment = new PostComment();
            postComment.setPostId(postId);
            postComment.setUserId(userId);
            postComment.setContent(content);
            return B.ok(commentService.save(postComment));
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public B<Boolean> delPost(IdRequest idRequest) {
        if (idRequest == null) {
            return B.parameter();
        }
        Long id = idRequest.getId();
        synchronized (id.toString().intern()) {
            if (LongUtil.isEmpty(id)) {
                return B.parameter();
            }
            User loginUser = UserUtils.getLoginUser();
            Post post = this.getById(id);
            if (post == null) {
                return B.parameter();
            }
            Long loginUserId = loginUser.getId();
            Long userId = post.getUserId();
            if (!loginUserId.equals(userId) && !UserRole.isAdmin(loginUser)) {
                return B.auth();
            }
            QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
            thumbQueryWrapper.eq("post_id", id);
            thumbService.remove(thumbQueryWrapper);
            QueryWrapper<PostCollect> postCollectQueryWrapper = new QueryWrapper<>();
            postCollectQueryWrapper.eq("post_id", id);
            collectService.remove(postCollectQueryWrapper);
            QueryWrapper<PostComment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq("post_id", id);
            commentService.remove(commentQueryWrapper);
            boolean remove = this.removeById(id);
            if (!remove) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
            }
            QueryWrapper<PostGroup> postGroupQueryWrapper = new QueryWrapper<>();
            postGroupQueryWrapper.eq("post_id", id);
            remove = postGroupService.remove(postGroupQueryWrapper);
            if (!remove) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
            }
        }

        return B.ok();
    }

    /**
     * 获取收藏数
     *
     * @return
     */
    @Override
    public B<Map<String, Object>> getPostByCollection() {
        User user = UserUtils.getLoginUser();
        Long id = user.getId();
        Map<String, Object> map = new HashMap<>(3);
        int totalPost = 0;
        int totalThumb = 0;
        List<PostUserVo> postUserVos = baseMapper.selectPostThumbTotal(Collections.singletonList(id));
        if (!CollectionUtils.isEmpty(postUserVos)) {
            PostUserVo postUserVo = postUserVos.get(0);
            totalPost = postUserVo.getPostTotal();
            totalThumb = postUserVo.getThumbTotal();

        }
        QueryWrapper<PostCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        long count = collectService.count(wrapper);

        map.put("totalCollect", count);
        map.put("totalPost", totalPost);
        map.put("totalThumb", totalThumb);
        return B.ok(map);
    }

    private boolean isUserTeam(Long userId, Long groupId) {
        boolean b = teamService.teamById(groupId);
        if (!b) {
            return false;
        }
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("team_id", groupId);
        long count = userTeamService.count(wrapper);
        return count == 1;
    }

    @Override
    public B<PostVo> getPost(Long postId) {
        if (LongUtil.isEmpty(postId)) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        PostVo postVo = baseMapper.selectPostUserOrderById(postId);
        if (postVo == null) {
            return B.parameter();
        }
        Long userId = loginUser.getId();
        Long groupId = postVo.getGroupId();
        if (groupId != 0) {
            if (!isUserTeam(userId, groupId)) {
                return B.parameter();
            }
        }
        List<CommentVo> commentVoList = baseMapper.getPostCommentByPostId(postId);
        for (CommentVo commentVo : commentVoList) {
            Long userID = commentVo.getOwner().getId();
            if (userID.equals(userId)) {
                commentVo.setCom(true);
            }
        }
        postVo.setCommentList(commentVoList);
        QueryWrapper<PostCollect> postCollectQueryWrapper = new QueryWrapper<>();
        postCollectQueryWrapper.eq("post_id", postId);
        postCollectQueryWrapper.eq("user_id", userId);
        long countCollect = collectService.count(postCollectQueryWrapper);
        if (countCollect == 1) {
            postVo.setHasCollect(true);
        }
        QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>();
        thumbQueryWrapper.eq("post_id", postId);
        thumbQueryWrapper.eq("user_id", userId);

        long countThumb = thumbService.count(thumbQueryWrapper);
        if (countThumb == 1) {
            postVo.setHasThumb(true);
        }
        rabbitService.postRead(userId, postId);
        return B.ok(postVo);
    }

    /**
     * 根据收藏数获取文章
     *
     * @return
     */
    @Override
    public B<List<PostVo>> getPostByCollect() {
        User loginUser = UserUtils.getLoginUser();
        Long userId = loginUser.getId();
        return B.ok(baseMapper.selectPostCollectByUserId(userId));
    }

    @Override
    public B<Map<String, Object>> getPostByRecord(PageRequest pageRequest) {
        User loginUser = UserUtils.getLoginUser();
        Long userId = loginUser.getId();
        QueryWrapper<PostRead> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("update_time");
        long pageNum = pageRequest.getCurrent();
        long pageSize = pageRequest.getSize();
        if (pageNum < 1 || pageSize > 10) {
            return B.parameter();
        }
        Page<PostRead> page = new Page<>(pageNum, pageSize);
        Page<PostRead> readPage = readService.page(page, wrapper);
        Map<String, Object> map = new HashMap<>();
        List<PostRead> records = readPage.getRecords();
        if (records.size() > 0) {
            List<Long> postIdList = records.stream().map(PostRead::getPostId).collect(Collectors.toList());
            List<PostVo> postVoList = baseMapper.selectPostVoByIds(postIdList);
            map.put("list", postVoList);
        }
        long total = readPage.getTotal();

        map.put("total", total);
        return B.ok(map);
    }

    @Override
    public B<List<PostVo>> searchPost(SearchPostRequest searchPostRequest) {
        if (searchPostRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = UserUtils.getLoginUser();
        String content = searchPostRequest.getContent();
        Long userId = searchPostRequest.getUserId();
        List<PostVo> postVoList = null;
        if (StringUtils.hasText(content)) {
            postVoList = baseMapper.searchContent(content);
            return B.ok(postVoList);
        } else if (userId != null && userId <= 0) {
            postVoList = baseMapper.searchUser(userId);
        }

        return B.ok(postVoList);
    }

    @Override
    public B<Boolean> delComment(DelCommRequest commRequest) {

        if (commRequest == null) {
            return B.parameter();
        }
        User user = UserUtils.getLoginUser();
        Long userId = user.getId();
        synchronized (userId.toString().intern()) {
            Long id = commRequest.getId();
            Long postId = commRequest.getPostId();
            if (postId == null || id == null) {
                return B.parameter();
            }
            PostComment comment = commentService.getById(id);
            if (comment == null) {
                return B.parameter();
            }
            Long commentUserId = comment.getUserId();
            Long commentPostId = comment.getPostId();
            if (commentPostId == null || commentUserId == null || commentPostId <= 0 || commentUserId <= 0) {
                return B.parameter();
            }
            if (!userId.equals(commentUserId) || !postId.equals(commentPostId)) {
                return B.auth();
            }
            return B.ok(commentService.removeById(comment.getId()));
        }
    }

    @Override
    public B<Map<String, Object>> imagePage(PageRequest pageRequest) {
        if (pageRequest == null) {
            return B.parameter();
        }
        long pageNum = pageRequest.getCurrent();
        long pageSize = pageRequest.getSize();
        if (pageNum <= 0 || pageSize > 10 || pageSize <= 0) {
            return B.parameter();
        }
        PageFilter filter = new PageFilter(pageNum, pageSize);
        Map<String, Object> map = new HashMap<>();
        long count = iImageService.count();
        List<Image> imageList = baseMapper.selectPageByImageDesc(filter.getCurrent(), filter.getSize());
        map.put("total", count);
        map.put("records", imageList);
        return B.ok(map);
    }

    @Override
    public B<PostListPageResponse> getPostTeamList(PostPageRequest postPageRequest) {
        if (postPageRequest == null) {
            return B.parameter();
        }
        long pageNum = postPageRequest.getCurrent();
        long pageSize = postPageRequest.getSize();
        String content = postPageRequest.getContent();
        Integer sorted = postPageRequest.getSorted();
        String tagId = postPageRequest.getTagId();
        PageFilter filter = new PageFilter(pageNum, pageSize);
        Long groupId = postPageRequest.getGroupId();
        User loginUser = UserUtils.getLoginUser();
        if (LongUtil.isEmpty(groupId)) {
            return B.parameter();
        }
        Long loginUserId = loginUser.getId();
        if (!isUserTeam(loginUserId, groupId)) {
            return B.parameter();
        }
        if (groupId == 0) {
            return B.parameter();
        }
        long total = 0;
        CompletableFuture<Long> completableFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectCountByGroupIdUserId(groupId, null), executorService);

        List<PostVo> postVoList = baseMapper.selectPostByUserOrderPage(filter.getCurrent(), filter.getSize(), sorted, null, groupId);
        postVoList = getPostVoList(postVoList);
        postVoList = getPostVoSorted(postPageRequest, postVoList);

        try {
            total = completableFuture.get();
        } catch (Exception e) {
            log.error("getPostTeamList completableFuture.get(); error: {}", e.getMessage());
        }
        return B.ok(PostListPageResponse.builder().records(postVoList).size(pageSize).current(pageNum).total(total).build());

    }

    /**
     * 这里要进行重排序
     *
     * @param postPageRequest
     * @param postVoList
     * @return
     */
    private List<PostVo> getPostVoSorted(PostPageRequest postPageRequest, List<PostVo> postVoList) {
        if (CollectionUtils.isEmpty(postVoList)) {
            return postVoList;
        }
        if (postPageRequest.getSorted() == 3) {
            postVoList = postVoList.stream().sorted((a, b) -> Integer.compare(b.getThumb(), a.getThumb())).collect(Collectors.toList());
        } else if (postPageRequest.getSorted() == 1) {
            postVoList = postVoList.stream().sorted((a, b) -> {
                LocalDateTime createTimeA = a.getCreateTime();
                LocalDateTime createTimeB = b.getCreateTime();
                return createTimeB.compareTo(createTimeA);
            }).collect(Collectors.toList());
        } else if (postPageRequest.getSorted() == 5) {
            //TODO
        }
        return postVoList;
    }

    @Override
    @RedissonLock
    public B<Boolean> deleteTeamById(Long teamId) {

        if (LongUtil.isEmpty(teamId)) {
            return B.parameter();
        }
        boolean team = deleteTeam(teamId);
        if (!team) {
            B.error();
        }
        return B.ok(team);


    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long id) {
        User loginUser = UserUtils.getLoginUser();
        Long userId = loginUser.getId();
        Team team = teamService.getById(id);
        if (team == null || LongUtil.isEmpty(team.getId())) {
            return false;
        }
        Long teamId = team.getId();
        Long teamUserId = team.getUserId();
        if (!userId.equals(teamUserId) || !UserRole.isAdmin()) {
            return false;
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        boolean remove = userTeamService.remove(userTeamQueryWrapper);
        if (!remove) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
        }
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("user_id", userId).and(wrapper -> wrapper.eq("id", teamId));
        boolean b = teamService.remove(teamQueryWrapper);
        if (!b) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
        }
        boolean record = teamChatRecordService.deleteTeamChatRecordByTeamId(teamId);
        if (!record) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
        }
        if (teamId == 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        List<String> postIds = baseMapper.selectPostIdsByGroupId(teamId);
        if (!CollectionUtils.isEmpty(postIds)) {
            boolean batchByIds = this.removeBatchByIds(postIds);
            if (!batchByIds) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
            }
            boolean t = thumbService.removeListByPostId(postIds);
            if (!t) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
            }
            boolean c = collectService.removeListByPostId(postIds);
            if (!c) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
            }
            boolean m = commentService.removeListByPostId(postIds);
            if (!m) {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "删除失败");
            }
        }

        return true;
    }

    @Override
    public B<PageResp<List<PostVo>>> getPostListByIndex(PostPageReq postPageReq) {
        if (PostPageReq.isEmpty(postPageReq)) {
            return B.parameter();
        }
        return B.ok(buildPost(postPageReq.getScope(), postPageReq.getCurrent(), postPageReq.getSize(), PostGroupEnum.INDEX.getValue(), null));
    }

    @Override
    public B<PageResp<List<PostVo>>> getPostTeamList(PostTeamPageReq teamPageReq) {
        if (PostPageReq.isEmpty(teamPageReq)) {
            return B.parameter();
        }
        Long tid = teamPageReq.getTid();
        if (LongUtil.isEmpty(tid)) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        boolean userTeam = teamService.isUserTeam(tid, loginUser.getId());
        if (!userTeam) {
            return B.parameter();
        }
        return B.ok(buildPost(teamPageReq.getScope(), teamPageReq.getCurrent(), teamPageReq.getSize(), tid, null));
    }

    @Override
    public B<PageResp<List<PostVo>>> getPostListByUser(PostTeamPageReq postTeamPageReq) {
        if (PostPageReq.isEmpty(postTeamPageReq)) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        Long loginUserId = loginUser.getId();
        return B.ok(buildPost(postTeamPageReq.getScope(), postTeamPageReq.getCurrent(), postTeamPageReq.size, postTeamPageReq.getTid(), loginUserId));
    }

    /**
     * @param scope   范围
     * @param current 页数
     * @param size    当前 大小
     * @param groupId 分组Id
     * @param userId  是否查询当前登录用户的文章
     * @return
     */
    public PageResp<List<PostVo>> buildPost(String scope, Long current, Long size, Long groupId, Long userId) {
        PageFilter pageFilter = new PageFilter(current, size);
        List<PostVo> postVos = buildPostSorted(scope, groupId, pageFilter
                , userId);
        postVos = buildPostCOMMENTAndTHUMB(postVos);
        return buildPostPage(postVos, groupId, userId, pageFilter);
    }

    /**
     * 构建Page
     *
     * @param postVos    数据
     * @param groupId    分组id
     * @param pageFilter page
     * @return
     */
    private PageResp<List<PostVo>> buildPostPage(List<PostVo> postVos, Long groupId, Long userId, PageFilter pageFilter) {
        if (LongUtil.isEmpty(groupId)) {
            groupId = PostGroupEnum.INDEX.getValue();
        }
        long total = baseMapper.selectCountByGroupIdUserId(groupId, userId);
        return new PageResp<>(!PageUtil.hasNext(pageFilter.getCurrent(), pageFilter.getSize(), total), postVos);
    }

    private List<PostVo> buildPostCOMMENTAndTHUMB(List<PostVo> postVos) {
        return getPostVoList(postVos);
    }

    /**
     * 查询 post list
     *
     * @param Scope      查询的范围
     * @param pageFilter 页面
     * @param groupId    分组Id
     * @param userId     是否查询登录的用户
     * @return PostVo
     */
    public List<PostVo> buildPostSorted(String Scope, Long groupId, PageFilter pageFilter, Long userId) {
        if (LongUtil.isEmpty(groupId)) {
            groupId = PostGroupEnum.INDEX.getValue();
        }
        if (!groupId.equals(PostGroupEnum.INDEX.getValue()) && !LongUtil.isEmpty(userId)) {
            boolean userTeam = teamService.isUserTeam(groupId, userId);
            if (!userTeam) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR);
            }
        }
        List<PostVo> postVos;
        PostSortedEnum postSortedEnum = PostSortedEnum.isScope(Scope);
        if (pageFilter == null) {
            pageFilter = new PageFilter();
        }
        switch (postSortedEnum) {
            // 最新
            case DESC:
                postVos = getPostPageByDESC(groupId, userId, pageFilter.getCurrent(), pageFilter.getSize());
                break;
            // 点赞最多
            case THUMB:
                postVos = getPostPageByTHUMB(groupId, userId, pageFilter.getCurrent(), pageFilter.getSize());
                break;
            // 评论最多
            case COMMENT:
                postVos = getPostPageByCOMMENT(groupId, userId, pageFilter.getCurrent(), pageFilter.getSize());
                break;
            // 推荐
            case RECOMMEND:
                postVos = getPostCursorByRECOMMEND(groupId, userId, pageFilter.getCurrent(), pageFilter.getSize());
                break;
            // 浏览次数最多
            case HOT:
                postVos = getPostPageByHot(groupId, userId, pageFilter.getCurrent(), pageFilter.getSize());
                break;
            default:
                log.error("buildPostSorted 参数错误 error: Scope=>{},postSortedEnum=>{}", Scope, postSortedEnum);
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        return postVos;
    }

    private List<PostVo> getPostPageByDESC(Long groupId, Long userId, long current, long size) {
        return baseMapper.getPostPageByDESC(groupId, userId, current, size);
    }

    private List<PostVo> getPostPageByTHUMB(Long groupId, Long userId, long current, long size) {
        return baseMapper.getPostPageByTHUMB(groupId, userId, current, size);
    }

    private List<PostVo> getPostPageByCOMMENT(Long groupId, Long userId, long current, long size) {
        return baseMapper.getPostPageByCOMMENT(groupId, userId, current, size);
    }

    private List<PostVo> getPostPageByHot(Long groupId, Long userId, long current, long size) {
        return baseMapper.getPostPageByHot(groupId, userId, current, size);
    }

    private List<PostVo> getPostCursorByRECOMMEND(Long groupId, Long userId, long current, long size) {
        List<PostVo> postVos = new ArrayList<>();
        if (!LongUtil.isEmpty(userId)) {
            return getPostPageByDESC(groupId, userId, current, size);
        }
        try {
            User loginUser = UserUtils.getLoginUser();
            Long loginUserId = loginUser.getId();
            String key = CacheConstants.MATCH_POST + loginUserId;
            if (redisCache.hasKey(key)) {
                Set<String> postIds = redisCache.getZSetDesc(key, current, size);
                postVos = baseMapper.selectPostVoByIds(postIds);
            }
            if (postVos.size() < size) {
                postVos.addAll(getPostPageByDESC(groupId, current, userId, size - postVos.size()));
            }
        } catch (Exception e) {
            postVos = getPostPageByDESC(groupId, userId, current, size);
        }
        return postVos;
    }
}
