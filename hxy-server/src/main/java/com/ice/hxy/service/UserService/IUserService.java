package com.ice.hxy.service.UserService;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.vo.UserAvatarVo;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.request.*;
import com.ice.hxy.mode.resp.SafetyUserResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
public interface IUserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userRegisterRequest 账户
     * @return token
     */
    B<String> userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLogin @return 用户信息
     * @return
     */
    B<String> userLogin(UserLoginRequest userLogin, HttpServletRequest request);


    /**
     * 用户注销
     *
     * @param request
     */
    B<Void> userLogout(HttpServletRequest request);

    /**
     * 修改用户
     *
     * @param user
     * @return
     */
    B<Integer> updateUser(User user);

    /**
     * ===============================================================
     * 根据标签搜索用户
     *
     * @return
     */
    B<List<SafetyUserResponse>> searchUserTag(UserSearchTagAndTxtRequest userSearchTagAndTxtRequest);

    /**
     * 修改用户
     *
     * @param updateUser 要修改的值
     * @return 4
     */
    B<String> getUserByUpdateID(UpdateUserRequest updateUser);

    B<Page<SafetyUserResponse>> friendUserName(UserSearchPage userSearchPage);

    B<Map<String, Object>> selectPageIndexList(long current, long size);


    /**
     * 根据单个标签搜索用户
     *
     * @param tag
     * @param request
     * @return
     */
    B<List<SafetyUserResponse>> searchUserTag(String tag, HttpServletRequest request);

    /**
     * 匹配
     *
     * @param num 数量
     * @return 数组
     */
    B<List<SafetyUserResponse>> matchUsers(long num);

    /**
     * 用户忘记密码
     *
     * @param registerRequest
     * @return
     */
    B<Boolean> userForget(UserRegisterRequest registerRequest);

    boolean seeUserEmail(String email);

    // 根据邮箱查找用户
    User forgetUserEmail(String email);

    List<UserAvatarVo> getUserAvatarVoByIds(List<Long> list);

    B<SafetyUserResponse> getCurrent(HttpServletRequest request);

    B<UserVo> getAdminCurrent(HttpServletRequest request);


    B<SafetyUserResponse> getUserVoByNameOrId(IdNameRequest idNameRequest);

    B<String> userAdminLogin(UserLoginRequest userLogin);

    B<String> qqLogin();

    B<String> getQQInfo(QQLoginRequest qqLoginRequest);
}
