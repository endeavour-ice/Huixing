package com.ice.hxy.controller.admin;

import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.admin.NoticeReq;
import com.ice.hxy.mode.request.admin.PostCookie;
import com.ice.hxy.mode.resp.admin.PostSortedResp;
import com.ice.hxy.mode.resp.tag.TagResp;
import com.ice.hxy.service.admin.SystemService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.AvatarUrlUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/11 10:10
 * @Description: 系统设置
 */
@RestController
@RequestMapping("/system")
public class SystemConfig {
    @PostConstruct
    public void init() {
        redisCache.setCacheList(CacheConstants.DEFAULT_AVATAR, AvatarUrlUtils.getUrl());
        if (!redisCache.hasKey(CacheConstants.HAS_CODE)) {
            redisCache.setCacheObject(CacheConstants.HAS_CODE, true);
        }
        redisCache.setCacheList(CacheConstants.DEFAULT_AVATAR_TEAM, AvatarUrlUtils.getTeam_url());
    }

    @Resource
    private RedisCache redisCache;

    @Resource
    private SystemService systemService;

    @PostMapping("/cookie")
    @AuthSecurity(isRole = {UserRole.ADMIN, UserRole.ROOT})
    public B<Boolean> postCookie(@RequestBody PostCookie cookie) {
        return systemService.postCookie(cookie);

    }

    @GetMapping("/cookie/get")
    @AuthSecurity(isRole = {UserRole.ADMIN, UserRole.ROOT})
    public B<List<PostCookie>> getCookie() {
        return systemService.getCookie();
    }

    @PostMapping("/uav")
    @AuthSecurity(isRole = {UserRole.ADMIN, UserRole.ROOT})
    public B<Boolean> defaultUserUrl(@RequestBody String av) {
        return systemService.defaultUserUrl(av);
    }

    @PostMapping("/tav")
    @AuthSecurity(isRole = {UserRole.ADMIN, UserRole.ROOT})
    public B<Boolean> defaultTeamUrl(@RequestBody String tav) {
        return systemService.defaultTeamUrl(tav);
    }

    @GetMapping("/up_url")
    @AuthSecurity(isRole = {UserRole.ADMIN, UserRole.ROOT})
    public B<Boolean> upUserAvUrl(@RequestParam(required = false) String cookie) {
        return systemService.upUserAvUrl(cookie);
    }

    @PostMapping("/notice")
    @AuthSecurity(isRole = {UserRole.ADMIN, UserRole.ROOT})
    public B<Boolean> notice(@RequestBody NoticeReq req) {
        return systemService.notice(req);
    }

    @GetMapping("/notice/get")
    @AuthSecurity(isRole = {UserRole.ADMIN, UserRole.ROOT})
    public B<String> getNotice() {
        return systemService.getNotice();
    }

    @GetMapping("/tag/get")
    @AuthSecurity(isRole = {UserRole.ADMIN, UserRole.ROOT})
    public B<TagResp> getTag() {
        return systemService.getTag();
    }

    @GetMapping("/up/post/dft")
    @AuthSecurity(isRole = {UserRole.ROOT})
    public B<Boolean> defaultPostIndex(@RequestParam("value") String value) {
        return systemService.defaultPostIndex(value);
    }
    @GetMapping("/get/post/dft")
    @AuthSecurity(isRole = {UserRole.ROOT})
    public B<List<PostSortedResp>> getDefaultPostIndex() {
        return systemService.getDefaultPostIndex();
    }
}
