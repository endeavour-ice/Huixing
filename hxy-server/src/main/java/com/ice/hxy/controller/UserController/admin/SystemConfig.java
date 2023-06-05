package com.ice.hxy.controller.UserController.admin;

import com.google.gson.reflect.TypeToken;
import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.AvatarUrlUtils;
import com.ice.hxy.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/11 10:10
 * @Description: 系统设置
 */
@RestController
public class SystemConfig {
    @PostConstruct
    public void init() {
        redisCache.setCacheList(CacheConstants.DEFAULT_AVATAR, AvatarUrlUtils.getUrl());
        if (!redisCache.hasKey(CacheConstants.HAS_CODE)) {
            redisCache.setCacheObject(CacheConstants.HAS_CODE, true);
        }
        redisCache.setCacheList(CacheConstants.DEFAULT_AVATAR_TEAM, AvatarUrlUtils.getTeam_url());
    }
    @Autowired
    private RedisCache redisCache;

    @GetMapping("/system/cookie")
    @AuthSecurity(isRole = {UserRole.ADMIN,UserRole.ROOT})
    public B<Boolean> postCookie(@RequestParam("cookie") String cookie) {
        if (!StringUtils.hasText(cookie)) {
            return B.error();
        }
        redisCache.setCacheObject(CacheConstants.ADD_POST_COOKIE_JOB, cookie);
        return B.ok(true);
    }


    @GetMapping("/system/code")
    @AuthSecurity(isRole = {UserRole.ADMIN,UserRole.ROOT})
    public B<Boolean> hasCode(@RequestParam("cookie") Boolean code) {
        if (code==null) {
            return B.error();
        }
        redisCache.setCacheObject(CacheConstants.ADD_POST_COOKIE_JOB, code);
        return B.ok(true);
    }
    @GetMapping("/system/getCode")
    @AuthSecurity(isRole = {UserRole.ADMIN,UserRole.ROOT})
    public B<Boolean> getCode() {
       Boolean b= redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB);
        return B.ok(b);
    }

    @PostMapping ("/system/uav")
    @AuthSecurity(isRole = {UserRole.ADMIN,UserRole.ROOT})
    public B<Boolean> setUserUrl(@RequestBody String av) {
        if (!StringUtils.hasText(av)) {
            return B.error(ErrorCode.PARAMS_ERROR);
        }
        try {
            List<String> list = GsonUtils.getGson().fromJson(av, new TypeToken<List<String>>() {
            }.getType());
            redisCache.setCacheList(CacheConstants.DEFAULT_AVATAR, list);
        } catch (Exception e) {
            return B.error(ErrorCode.ERROR);
        }
        return B.ok(true);
    }
    @PostMapping("/system/tav")
    @AuthSecurity(isRole = {UserRole.ADMIN,UserRole.ROOT})
    public B<Boolean> setTeamUrl(@RequestBody String tav) {
        if (!StringUtils.hasText(tav)) {
            return B.error(ErrorCode.PARAMS_ERROR);
        }
        try {
            List<String> list = GsonUtils.getGson().fromJson(tav, new TypeToken<List<String>>() {
            }.getType());
            redisCache.setCacheList(CacheConstants.DEFAULT_AVATAR_TEAM, list);
        } catch (Exception e) {
            return B.error(ErrorCode.ERROR);
        }
        return B.ok(true);
    }

}
