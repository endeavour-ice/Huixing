package com.ice.hxy.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.enums.UserStatus;
import com.ice.hxy.mode.resp.SafetyUserResponse;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ice
 * @date 2022/8/23 11:49
 */

public class UserUtils {
    private static final ThreadLocal<User> threadLocal = new ThreadLocal<>();
    /**
     * 通过解析获取用户信息
     * @return 用户信息
     */
    public static User getLoginUser() {
        User user = threadLocal.get();
        if (user == null|| user.getId()==null || user.getId()<=0) {
            throw new GlobalException(ErrorCode.NO_LOGIN);
        }
        return user;
    }
    public static void removeLocal() {
        threadLocal.remove();
    }
    public static void setLoginUser(User user) {
        if (user != null) {
            threadLocal.set(user);
        }else {
            removeLocal();
        }

    }
    public static UserVo getSafetyUser(User user) {
        if (user == null) {
            return null;
        }
        UserVo cleanUser = new UserVo();
        cleanUser.setId(user.getId());
        cleanUser.setUsername(user.getUsername());
        cleanUser.setUserAccount(user.getUserAccount());
        cleanUser.setAvatarUrl(user.getAvatarUrl());
        cleanUser.setGender(user.getGender());
        cleanUser.setTel(user.getTel());
        cleanUser.setEmail(user.getEmail());
        cleanUser.setUserStatus(user.getUserStatus());
        cleanUser.setCreateTime(user.getCreateTime());
        cleanUser.setRole(user.getRole());
        cleanUser.setPlanetCode(user.getPlanetCode());
        cleanUser.setTags(user.getTags());
        cleanUser.setProfile(user.getProfile());
        return cleanUser;
    }
    public static SafetyUserResponse getSafetyUserResponse(User user) {
        if (user == null) {
            return null;
        }
        SafetyUserResponse safetyUserResponse = new SafetyUserResponse();
        safetyUserResponse.setId(user.getId());
        safetyUserResponse.setUsername(user.getUsername());
        safetyUserResponse.setUserAccount(user.getUserAccount());
        safetyUserResponse.setAvatarUrl(user.getAvatarUrl());
        safetyUserResponse.setGender(user.getGender());
        safetyUserResponse.setTags(user.getTags());
        safetyUserResponse.setProfile(user.getProfile());
        safetyUserResponse.setTel(user.getTel());
        Integer status = user.getUserStatus();
        if ( status== UserStatus.NORMAL.getKey()) {
            safetyUserResponse.setStatus("公开");
        } else if (status == UserStatus.PRIVATE.getKey()) {
            safetyUserResponse.setStatus("私密");
        }
        safetyUserResponse.setEmail(user.getEmail());
        return safetyUserResponse;
    }

    public static Page<SafetyUserResponse> getPageVo(Page<User> page) {
        if (page == null) {
            return null;
        }
        List<User> records = page.getRecords();
        List<SafetyUserResponse> userVoList = records.stream().map(UserUtils::getSafetyUserResponse).collect(Collectors.toList());
        Page<SafetyUserResponse> voPage = new Page<>();
        voPage.setRecords(userVoList);
        voPage.setTotal(page.getTotal());
        voPage.setSize(page.getSize());
        voPage.setCurrent(page.getCurrent());
        voPage.setSearchCount(page.searchCount());
        voPage.setOptimizeCountSql(page.optimizeCountSql());
        voPage.setOrders(page.orders());
        voPage.setOptimizeJoinOfCountSql(page.optimizeJoinOfCountSql());
        voPage.setCountId(page.countId());
        voPage.setMaxLimit(page.maxLimit());
        return voPage;

    }
}
