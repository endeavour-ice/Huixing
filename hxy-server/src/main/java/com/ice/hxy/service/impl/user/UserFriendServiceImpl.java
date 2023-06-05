package com.ice.hxy.service.impl.user;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mapper.UserFriendMapper;
import com.ice.hxy.mapper.UserMapper;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.UserFriend;
import com.ice.hxy.mode.entity.UserFriendReq;
import com.ice.hxy.mode.entity.vo.UserAvatarVo;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.request.AddFriendUSerUser;
import com.ice.hxy.mode.request.RejectRequest;
import com.ice.hxy.mode.resp.FriendResponse;
import com.ice.hxy.mode.resp.FriendUserResponse;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.service.UserService.IUserFriendReqService;
import com.ice.hxy.service.UserService.IUserFriendService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.util.LongUtil;
import com.ice.hxy.util.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ice.hxy.mode.constant.UserConstant.ASSISTANT_ID;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@Service
public class UserFriendServiceImpl extends ServiceImpl<UserFriendMapper, UserFriend> implements IUserFriendService {
    @Resource
    private IUserService userService;
    @Autowired
    private UserMapper userMapper;

    @Resource
    private IUserFriendReqService userFriendReqService;
    @Resource
    private RedisCache redisCache;

    @Override
    public List<FriendResponse> selectFriend(Long userId) {
        List<Long> userIdByList = selectFriendRId(userId);
        if (!CollectionUtils.isEmpty(userIdByList)) {
            List<UserAvatarVo> avatarVos = userMapper.getUserAvatarVoByIds(userIdByList);
            List<FriendResponse> list = new ArrayList<>();
            for (UserAvatarVo avatarVo : avatarVos) {
                FriendResponse response = new FriendResponse();
                Long id = avatarVo.getId();
                response.setId(id);
                response.setAvatarUrl(avatarVo.getAvatarUrl());
                response.setUsername(avatarVo.getUsername());
                String key = CacheConstants.IS_LOGIN + id;
                if (ASSISTANT_ID.equals(id)) {
                    response.setOnline(true);
                }else {
                    response.setOnline(redisCache.hasKey(key));
                }

                list.add(response);
            }
            return list;
        }
        return null;
    }

    @Override
    public List<Long> selectFriendRId(Long userId) {
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).or().eq("friend_id", userId);
        List<UserFriend> userFriends = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(userFriends)) {
            return null;
        }
        List<Long> userIdByList = new ArrayList<>();
        userFriends.forEach(userFriend -> {
            if (userFriend.getUserId().equals(userId)) {
                userIdByList.add(userFriend.getFriendId());
            }
            if (userFriend.getFriendId().equals(userId)) {
                userIdByList.add(userFriend.getUserId());
            }
        });

        return userIdByList;
    }

    @Override
    public FriendUserResponse getFriendUser(Long friendId, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser();
        if (LongUtil.isEmpty(friendId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        Long userId = loginUser.getId();
        UserFriend userFriend = this.getUserFriendByFriendId(friendId, userId);
        if (userFriend == null) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        LocalDateTime createTime = userFriend.getCreateTime();
        User feignUser = userService.getById(friendId);
        if (feignUser == null) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        FriendUserResponse response = new FriendUserResponse();
        BeanUtils.copyProperties(feignUser, response);
        response.setCreateTime(createTime);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delFriendUser(Long friendId, Long userId) {
        if (LongUtil.isEmpty(friendId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        UserFriend userFriend = this.getUserFriendByFriendId(friendId, userId);
        if (userFriend == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        boolean removeById = this.removeById(userFriend);
        if (!removeById) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数错误...");
        }
        Integer is = baseMapper.removeChatRecord(userId, friendId);
        if (is == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return true;
    }

    private UserFriend getUserFriendByFriendId(Long friendId, Long userId) {
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).and(q -> q.eq("friend_id", friendId)).
                or().eq("user_id", friendId).and(q -> q.eq("friend_id", userId));
        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public Long sendRequest(Long userId, AddFriendUSerUser friendUSerUser) {
        if (friendUSerUser == null) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        String message = friendUSerUser.getMessage();
        Long toUserId = friendUSerUser.getFid();
        if (LongUtil.isEmpty(userId) || LongUtil.isEmpty(toUserId)) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空,请重试");
        }
        if (userId.equals(toUserId)) {
            throw new GlobalException(ErrorCode.ERROR, "无法添加自己");
        }
        synchronized (userId.toString().intern()) {
            User user = userService.getById(toUserId);
            if (user == null) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空,请重试");
            }
            boolean friend = isUserFriend(userId, toUserId);
            if (friend) {
                throw new GlobalException(ErrorCode.ERROR, "重复添加好友");
            }
            QueryWrapper<UserFriendReq> wrapper = new QueryWrapper<>();
            wrapper.eq("from_userid", userId);
            wrapper.eq("to_userid", toUserId);
            long count = userFriendReqService.count(wrapper);
            if (count > 0) {
                throw new GlobalException(ErrorCode.NULL_ERROR, "发送成功");
            }
            int countFromToUserId = baseMapper.countFromToUserId(toUserId, userId);
            if (countFromToUserId > 0) {
                int con = baseMapper.removeFromToUserId(toUserId, userId);
                if (con <= 0) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR);
                } else {
                    UserFriend userFriend = new UserFriend();
                    userFriend.setUserId(toUserId);
                    userFriend.setFriendId(userId);
                    boolean save = this.save(userFriend);
                    if (!save) {
                        throw new GlobalException(ErrorCode.PARAMS_ERROR);
                    } else {
                        return 1L;
                    }
                }
            } else {

                UserFriendReq userFriendReq = new UserFriendReq();
                userFriendReq.setFromUserid(userId);
                userFriendReq.setToUserid(toUserId);
                if (StringUtils.hasText(message)) {
                    userFriendReq.setMessage(message);
                }
                boolean insert = userFriendReqService.save(userFriendReq);
                if (!insert) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR, "发送失败");
                } else {
                    return 0L;
                }
            }
        }


    }

    @Override
    public List<UserVo> checkFriend(Long toUserId) {
        if (LongUtil.isEmpty(toUserId)) {
            return null;
        }
        List<UserFriendReq> friendReqList = baseMapper.selectCheckFriend(toUserId);
        //List<UserFriendReq> friendReqList = userFriendReqService.list(wrapper);
        if (friendReqList == null || friendReqList.size() <= 0) {
            return null;
        }

        Map<Long, List<UserFriendReq>> userFriendMap = friendReqList.stream().collect(Collectors.groupingBy(UserFriendReq::getFromUserid));
        List<User> users = userService.listByIds(userFriendMap.keySet());
        if (users.isEmpty()) {
            throw new RuntimeException("查找申请的用户为空");
        }
        return users.stream().map(user -> {
            UserVo userVo = UserUtils.getSafetyUser(user);
            userVo.setProfile(null);
            List<UserFriendReq> list = userFriendMap.get(userVo.getId());
            if (!CollectionUtils.isEmpty(list)) {
                String message = list.get(0).getMessage();
                if (StringUtils.hasText(message)) {
                    userVo.setProfile(message);
                }
            }
            return userVo;
        }).collect(Collectors.toList());

    }

    @Override
    @Transactional
    public boolean reject(RejectRequest rejectRequest, Long userId) {
        if (rejectRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        synchronized (userId.toString().intern()) {
            Long acceptId = rejectRequest.getAcceptId();
            Long refuseId = rejectRequest.getRefuseId();
            if (!LongUtil.isEmpty(acceptId)) {
                int count = baseMapper.countFromToUserId(acceptId, userId);
                if (count <= 0) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请求失败");
                }
                int toUserId = baseMapper.removeFromToUserId(acceptId, userId);
                if (toUserId <= 0) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR);
                } else {
                    if (isUserFriend(userId, acceptId)) {
                        throw new GlobalException(ErrorCode.ERROR, "重复添加好友");
                    }
                    UserFriend userFriend = new UserFriend();
                    userFriend.setUserId(userId);
                    userFriend.setFriendId(acceptId);
                    boolean save = this.save(userFriend);
                    if (!save) {
                        throw new GlobalException(ErrorCode.PARAMS_ERROR);
                    }
                }
            } else if (!LongUtil.isEmpty(refuseId)) {
                int count = baseMapper.countFromToUserId(acceptId, userId);
                if (count <= 0) {
                    throw new GlobalException(ErrorCode.NULL_ERROR, "请求失败");
                }
                int toUserId = baseMapper.removeFromToUserId(refuseId, userId);
                if (toUserId <= 0) {
                    throw new GlobalException(ErrorCode.PARAMS_ERROR);
                }
            } else {
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
            }
        }
        return true;
    }

    public boolean isUserFriend(Long userId, Long friendId) {
        QueryWrapper<UserFriend> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("user_id", userId).and(w -> w.eq("friend_id", friendId))
                .or().eq("user_id", friendId).and(w -> w.eq("friend_id", userId));
        long size = this.count(wrapper);
        return size > 0;
    }
}
