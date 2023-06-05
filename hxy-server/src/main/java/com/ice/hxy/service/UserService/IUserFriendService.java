package com.ice.hxy.service.UserService;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.mode.entity.UserFriend;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.request.AddFriendUSerUser;
import com.ice.hxy.mode.request.RejectRequest;
import com.ice.hxy.mode.resp.FriendResponse;
import com.ice.hxy.mode.resp.FriendUserResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
public interface IUserFriendService extends IService<UserFriend> {


    /**
     * 查找好友
     * @param userId
     * @return 用户
     */
    List<FriendResponse> selectFriend(Long userId);
    /**
     * 查找好友
     * @param userId
     * @return 用户ID
     */
    List<Long> selectFriendRId(Long userId);

    FriendUserResponse getFriendUser(Long friendId, HttpServletRequest request);

    /**
     * 删除好友
     * @param friendId
     * @param userId
     * @return
     */
    boolean delFriendUser(Long friendId, Long userId);

    Long sendRequest(Long fromUserId, AddFriendUSerUser toUserId);

    List<UserVo> checkFriend(Long userId);

    boolean reject(RejectRequest rejectRequest, Long userId);

    boolean isUserFriend(Long userId, Long friendId);
}
