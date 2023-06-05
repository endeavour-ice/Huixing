package com.ice.hxy.controller.UserController;


import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.comm.ChinesePoetry;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.ChinesePoetryUtil;
import com.ice.hxy.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * 公告表 前端控制器
 * </p>
 *
 * @author ice
 * @since 2022-09-18
 */
@RestController
@RequestMapping("/notice")
public class UserNoticeController {

    @Autowired
    private RedisCache redisCache;

    @GetMapping("/get")
    @CurrentLimiting
    public B<List<String>> get() {
        if (redisCache.hasKey(CacheConstants.NOTICE)) {
            return B.ok(redisCache.getCacheObject(CacheConstants.NOTICE));
        } else {
            ChinesePoetry randomPoetry = ChinesePoetryUtil.getRandomPoetry();
            if (randomPoetry == null) {
                return B.ok();
            }
            String author = randomPoetry.getAuthor();
            String rhythmic = randomPoetry.getRhythmic();
            String heard = "《" + rhythmic + "》 作者: " + author;
            List<String> list = new ArrayList<>();
            list.add(heard);
            list.addAll(randomPoetry.getParagraphs());
            return B.ok(list);
        }
    }

    @PostMapping("/save")
    public B<Void> save(String notice) {
        User loginUser = UserUtils.getLoginUser();
        if (!StringUtils.hasText(notice)) {
            return B.parameter();
        }

        if (!UserRole.isAdmin(loginUser)) {
            return B.auth();
        }
        redisCache.setCacheObject(CacheConstants.NOTICE, notice);
        return B.ok();
    }
}
