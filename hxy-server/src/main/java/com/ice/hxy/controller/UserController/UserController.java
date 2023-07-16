package com.ice.hxy.controller.UserController;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.*;
import com.ice.hxy.mode.resp.SafetyUserResponse;
import com.ice.hxy.mq.RabbitService;
import com.ice.hxy.service.UserService.IUserFriendService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.util.LongUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 缓存一致性 可以使用 Canal Java => https://blog.csdn.net/a56546/article/details/125170510
 * 数据库分库分表 读写分离 Mycat => https://blog.csdn.net/K_520_W/article/details/123702217
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@RestController
@RequestMapping("/api/user")
@SuppressWarnings("all")
@Slf4j
public class UserController {
    @Autowired
    private IUserFriendService friendService;

    @Resource
    private IUserService userService;

    @Autowired(required = false)
    private RabbitService rabbitService;

    @PostMapping("/register")
    public B<String> userRegister(@RequestBody UserRegisterRequest userRegister) {
        return userService.userRegister(userRegister);
    }

    @GetMapping("/current")
    @CurrentLimiting
    public B<SafetyUserResponse> getCurrent(HttpServletRequest request) {
        return userService.getCurrent(request);
    }

    // 用户登录
    @PostMapping("/login")
    public B<String> userLogin(@RequestBody UserLoginRequest userLogin, HttpServletRequest request) {
        return userService.userLogin(userLogin, request);
    }

    // 忘记密码
    @PostMapping("/forget")
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<Boolean> userForget(@RequestBody UserRegisterRequest registerRequest) {
        return userService.userForget(registerRequest);

    }


    // 管理员删除用户
    @PostMapping("/delete")
    @AuthSecurity(isRole = {UserRole.ADMIN})
    public B<Boolean> deleteUser(@RequestBody IdRequest idRequest) {

        if (idRequest == null ) {
            return B.parameter();
        }
        Long id = idRequest.getId();
        if (LongUtil.isEmpty(id)) {
            return B.parameter();
        }
        boolean admin = UserRole.isAdmin();
        if (!admin) {
            return B.auth();
        }
        boolean removeById = userService.removeById(id);
        return B.ok(removeById);
    }

    @PostMapping("/UpdateUser")
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<Integer> UpdateUser(User user) {
        return userService.updateUser(user);
    }


    /**
     * 用户注销
     */
    @PostMapping("/Logout")
    public B<Void> userLogout(HttpServletRequest request) {
        return userService.userLogout(request);

    }


    @PostMapping("/search/tags/txt")
    public B<List<SafetyUserResponse>> getSearchUserTag(@RequestBody UserSearchTagAndTxtRequest userSearchTagAndTxtRequest) {
        return userService.searchUserTag(userSearchTagAndTxtRequest);
    }

    /**
     * 修改用户
     */
    @PostMapping("/update")
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<String> updateUserByID(@RequestBody UpdateUserRequest updateUser) {
        return userService.getUserByUpdateID(updateUser);
    }

    /**
     * 主页展示数据
     */
    @GetMapping("/recommend")
    public B<Map<String, Object>> recommendUser(@RequestParam(required = false) long current, long size) {
        return userService.selectPageIndexList(current, size);
    }

    // 搜索用户
    @PostMapping("/search")
    public B<Page<SafetyUserResponse>> searchUserName(@RequestBody UserSearchPage userSearchPage) {
        return userService.friendUserName(userSearchPage);
    }


    /**
     * 根据单个标签搜索
     */
    @GetMapping("/search/tag")
    public B<List<SafetyUserResponse>> searchUserTag(@RequestParam("tag") String tag, HttpServletRequest request) {
        return userService.searchUserTag(tag, request);
    }

    /**
     * 匹配用户
     *
     * @param num     推荐数量
     * @param request
     * @return
     */
    @GetMapping("/match")
    public B<List<SafetyUserResponse>> matchUsers(long num) {
        return userService.matchUsers(num);

    }

    @PostMapping("/user")
    public B<SafetyUserResponse> getUserVoByNameOrId(@RequestBody IdNameRequest idNameRequest) {
        return userService.getUserVoByNameOrId(idNameRequest);
    }

    // 用户登录
    @PostMapping("/admin/Login")
    public B<String> userAdminLogin(@RequestBody UserLoginRequest userLogin, HttpServletRequest request) {
        return userService.userAdminLogin(userLogin);
    }

    /**
     * 获取当前的登录信息
     *
     * @return 返回用户
     */
    @GetMapping("/admin/current")
    @CurrentLimiting
    public B<UserVo> getAdminCurrent(HttpServletRequest request) {
        return userService.getAdminCurrent(request);
        // 进行脱敏
    }

    @GetMapping("/qq/login")
    public B<String> qqLogin() {
        return userService.qqLogin();
    }

    @PostMapping("/qq/getInfo")
    public B<String> getInfo(@RequestBody QQLoginRequest qqLoginRequest) {
        return userService.getQQInfo(qqLoginRequest);
    }
}
