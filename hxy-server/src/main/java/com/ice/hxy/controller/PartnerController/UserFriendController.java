package com.ice.hxy.controller.PartnerController;


import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.AddFriendUSerUser;
import com.ice.hxy.mode.request.RejectRequest;
import com.ice.hxy.mode.resp.FriendResponse;
import com.ice.hxy.mode.resp.FriendUserResponse;
import com.ice.hxy.service.UserService.IUserFriendService;
import com.ice.hxy.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-07-28
 */
@RestController
@RequestMapping("/friend")
//@CrossOrigin(origins = {"http://localhost:7777"}, allowCredentials = "true")
@Slf4j
public class UserFriendController {
    @Autowired
    private IUserFriendService friendService;


    // 添加好友
    @PostMapping("/add")
    public B<Long> friendRequest(@RequestBody AddFriendUSerUser addFriendUSerUser) {
        if (addFriendUSerUser == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        User loginUser = UserUtils.getLoginUser();
        Long userId = loginUser.getId();
        Long aLong = friendService.sendRequest(userId, addFriendUSerUser);
        return B.ok(aLong);
    }


    // 查看好友申请
    @GetMapping("/check")
    public B<List<UserVo>> CheckFriendRequests() {
        User user = UserUtils.getLoginUser();
        Long userId = user.getId();
        List<UserVo> users = friendService.checkFriend(userId);
        return B.ok(users);
    }

    /**
     * 接受和拒绝好友
     *
     * @param rejectRequest
     * @return
     */
    @PostMapping("/reject")
    public B<Boolean> rejectFriend(@RequestBody(required = false) RejectRequest rejectRequest, HttpServletRequest request) {
        User loginUser = UserUtils.getLoginUser();
        boolean i = friendService.reject(rejectRequest, loginUser.getId());
        return B.ok();
    }


    /**
     * 查找好友
     */
    @GetMapping("/select")
    public B<List<FriendResponse>> selectFriendList() {
        User user = UserUtils.getLoginUser();
        Long userId = user.getId();
        List<FriendResponse> userList = friendService.selectFriend(userId);
        return B.ok(userList);
    }

    /**
     * 查看好友详情
     *
     * @param friendId
     * @return
     */
    @GetMapping("/details")
    public B<FriendUserResponse> getFriendUser(@RequestParam("friendId") Long friendId, HttpServletRequest request) {
        FriendUserResponse friendUser = friendService.getFriendUser(friendId, request);
        return B.ok(friendUser);
    }

    /**
     * 删除好友
     */
    @GetMapping("/del")
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<Boolean> delFriendUser(@RequestParam("friendId") Long friendId) {
        User loginUser = UserUtils.getLoginUser();
        Long userId = loginUser.getId();
        boolean is = friendService.delFriendUser(friendId, userId);
        if (!is) {
            return B.error();
        }

        return B.ok();
    }
}
