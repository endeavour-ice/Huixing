package com.ice.hxy.service.admin.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.annotation.RedissonLock;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.admin.AdminSafetyUserResp;
import com.ice.hxy.mode.entity.admin.AdminUserResp;
import com.ice.hxy.mode.enums.LoginType;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.enums.UserStatus;
import com.ice.hxy.mode.request.UserLoginRequest;
import com.ice.hxy.mode.request.UserSearchPage;
import com.ice.hxy.mode.request.admin.UserAuthReq;
import com.ice.hxy.mode.request.admin.UserStatusReq;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.admin.AdminUserService;
import com.ice.hxy.service.commService.TokenService;
import com.ice.hxy.util.DateUtils;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.util.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/7/9 18:02
 * @Description: TODO
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Resource
    private IUserService userService;

    @Resource
    private TokenService tokenService;

    @Override
    public B<Page<AdminUserResp>> list(UserSearchPage searchPage) {
        if (searchPage == null) {
            return B.empty();
        }
        long current = searchPage.getCurrent();
        long size = searchPage.getSize();
        String userName = searchPage.getUserName();
        if (size > 20 || size <= 0) {
            return B.empty();
        }

        //if (!UserRole.isRoot(UserUtils.getLoginUser())) {
        //    return B.auth();
        //}
        Page<User> userPage = new Page<>(current, size);
        Page<User> page = userService.lambdaQuery().like(StringUtils.hasText(userName), User::getUserAccount, userName).page(userPage);
        Page<AdminUserResp> userVoPage = new Page<>();
        BeanUtils.copyProperties(userPage, userVoPage);
        userVoPage.setRecords(page.getRecords().stream().map(this::buildAdminUserResp).collect(Collectors.toList()));
        return B.ok(userVoPage);
    }

    public AdminUserResp buildAdminUserResp(User user) {
        AdminUserResp adminUserResp = new AdminUserResp();
        adminUserResp.setId(user.getId());
        adminUserResp.setUsername(user.getUsername());
        adminUserResp.setUserAccount(user.getUserAccount());
        adminUserResp.setLoginType(LoginType.getType(user.getLoginType()));
        adminUserResp.setAvatarUrl(user.getAvatarUrl());
        adminUserResp.setGender(user.getGender());
        String tags = user.getTags();
        if (StringUtils.hasText(tags)) {
            List<String> list;
            try {
                list = GsonUtils.getGson().fromJson(tags, new TypeToken<List<String>>() {
                }.getType());
                adminUserResp.setTags(list);
            } catch (Exception ignored) {

            }

        }

        adminUserResp.setTel(user.getTel());
        adminUserResp.setEmail(user.getEmail());
        adminUserResp.setUserStatus(UserStatus.getN(user.getUserStatus()));
        adminUserResp.setRole(UserRole.getN(user.getRole()));
        adminUserResp.setCreateTime(DateUtils.getLDTString(user.getCreateTime()));
        return adminUserResp;
    }

    @Override
    @RedissonLock
    public B<Boolean> upRole(UserAuthReq userAuthReq) {
        if (userAuthReq == null) {
            return B.parameter();
        }
        int id = userAuthReq.getId();
        if (id <= 0) {
            return B.parameter();
        }
        String auth = userAuthReq.getRole();
        if (!StringUtils.hasText(auth)) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        if (!UserRole.isRoot(loginUser)) {
            return B.auth();
        }
        User user = userService.getById(id);
        if (user == null) {
            return B.parameter();
        }
        if (Objects.equals(user.getRole(), loginUser.getRole())) {
            return B.ok();
        }
        for (UserRole role : UserRole.values()) {
            String name = role.getName();
            if (name.equals(auth)) {
                user.setRole(role.getKey());
                return B.ok(userService.updateById(user));
            }
        }
        return B.parameter();
    }

    @Override
    @RedissonLock
    public B<Boolean> upStatus(UserStatusReq userStatusReq) {
        if (userStatusReq == null) {
            return B.parameter();
        }
        int id = userStatusReq.getId();
        String status = userStatusReq.getStatus();
        if (id <= 0) {
            return B.parameter();
        }

        if (!StringUtils.hasText(status)) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();

        if (!UserRole.isRoot(loginUser)) {
            return B.auth();
        }
        User user = userService.getById(id);

        if (user == null) {
            return B.parameter();
        }
        if (Objects.equals(user.getRole(), loginUser.getRole())) {
            return B.ok();
        }
        for (UserStatus st : UserStatus.values()) {
            String name = st.getName();
            if (name.equals(status)) {
                user.setUserStatus(st.getKey());
                return B.ok(userService.updateById(user));
            }

        }
        return B.parameter();
    }

    @Override
    public B<String> userAdminLogin(UserLoginRequest userLogin) {
        return userService.userAdminLogin(userLogin);
    }

    @Override
    public B<AdminSafetyUserResp> getCurrent(HttpServletRequest request) {
        User user = tokenService.getTokenUser(request);
        if (user == null) {
            return B.auth();
        }
        String role = user.getRole();
        if (!role.equals(UserRole.ROOT.getKey()) && !role.equals(UserRole.ADMIN.getKey())) {
            return B.auth();
        }
        AdminSafetyUserResp adminSafetyUserResp = new AdminSafetyUserResp();
        adminSafetyUserResp.setRole(user.getRole());
        adminSafetyUserResp.setId(user.getId());
        adminSafetyUserResp.setUsername(user.getUsername());
        adminSafetyUserResp.setUserAccount(user.getUserAccount());
        adminSafetyUserResp.setAvatarUrl(user.getAvatarUrl());
        adminSafetyUserResp.setGender(user.getGender());
        adminSafetyUserResp.setTags(user.getTags());
        adminSafetyUserResp.setProfile(user.getProfile());
        adminSafetyUserResp.setTel(user.getTel());
        adminSafetyUserResp.setEmail(user.getEmail());
        adminSafetyUserResp.setStatus(UserStatus.getN(user.getUserStatus()));
        return B.ok(adminSafetyUserResp);
    }

    public B RefreshUserImage() {
        User loginUser = UserUtils.getLoginUser();

        if (!UserRole.isRoot(loginUser)) {
            return B.auth();
        }

        return B.ok();
    }
}
