package com.ice.hxy.service.admin.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.annotation.RedissonLock;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.enums.UserStatus;
import com.ice.hxy.mode.request.UserSearchPage;
import com.ice.hxy.mode.request.admin.UserAuthReq;
import com.ice.hxy.mode.request.admin.UserStatusReq;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.admin.UserService.AdminUserService;
import com.ice.hxy.util.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
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

    @Override
    public B<Page<UserVo>> list(UserSearchPage searchPage) {
        if (searchPage == null) {
            return B.empty();
        }
        long current = searchPage.getCurrent();
        long size = searchPage.getSize();
        String userName = searchPage.getUserName();
        if (size > 20||size<=0) {
            return B.empty();
        }

        if (!UserRole.isRoot(UserUtils.getLoginUser())) {
            return B.auth();
        }
        Page<User> userPage = new Page<>(current,size);
        Page<User> page = userService.lambdaQuery().like(StringUtils.hasText(userName), User::getUserAccount, userName).page(userPage);
        Page<UserVo> userVoPage = new Page<>();
        BeanUtils.copyProperties(userPage, userVoPage);
        userVoPage.setRecords(page.getRecords().stream().map(UserUtils::getSafetyUser).collect(Collectors.toList()));
        return B.ok(userVoPage);
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
        for (UserStatus st: UserStatus.values()) {
            String name = st.getName();
            if (name.equals(status)) {
                user.setUserStatus(st.getKey());
                return B.ok(userService.updateById(user));
            }

        }
        return B.parameter();
    }

    public B RefreshUserImage() {
        User loginUser = UserUtils.getLoginUser();

        if (!UserRole.isRoot(loginUser)) {
            return B.auth();
        }

        return B.ok();
    }
}
