package com.ice.hxy.service.admin.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.commService.HttpService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.LongUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
class SystemServiceImplTest {

    @Resource
    private RedisCache redisCache;
    @Resource
    private HttpService httpService;
    @Resource
    private TagsService tagsService;
    @Resource
    private ExecutorService executorService;
    @Resource
    private IUserService userService;
    @Test
    void upUserAvUrl() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        String cookie="";
        if (!StringUtils.hasText(cookie)) {
            cookie = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ);
        }
        List<User> list;
        List<Long> cacheList = redisCache.getCacheList("upUserAvUrl:fail");
        if (cacheList != null && cacheList.size() > 0) {
            list = userService.listByIds(cacheList);
        } else {
            list = userService.lambdaQuery().ge(User::getId, 10L).select(User::getId).list();
        }

        List<User> upUserList = new ArrayList<>(6001);
        List<Long> failList = new ArrayList<>();
        String url = "https://api.zsxq.com/v2/users/";
        for (User user : list) {
            Long id = user.getId();
            if (LongUtil.isEmpty(id)) {
                continue;
            }
            String completeUrl = url + user.getId();
            JSONObject parseObj;
            try {
                String body = get(completeUrl, cookie);
                if (StringUtils.hasText(body)) {
                    failList.add(id);
                    continue;
                }
                parseObj = JSONUtil.parseObj(body);
            } catch (Exception e) {
                failList.add(id);
                continue;
            }
            String resp_data = parseObj.getStr("resp_data");
            if (resp_data == null) {
                failList.add(id);
                continue;
            }
            JSONObject obj = JSONUtil.parseObj(resp_data);
            String user1 = obj.getStr("user");
            if (user1 == null) {
                failList.add(id);
                continue;
            }
            JSONObject parseObj1 = JSONUtil.parseObj(user1);
            String avatar_url = parseObj1.getStr("avatar_url");
            if (avatar_url == null) {
                failList.add(id);
                continue;
            }
            user.setAvatarUrl(avatar_url);
            user.setId(id);
            upUserList.add(user);

            if (upUserList.size() >= 6000) {
                List<User> finalUpUserList = upUserList;
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                    boolean batch = userService.updateBatchById(finalUpUserList);
                    if (!batch) {
                        log.error("失败");
                        failList.addAll(finalUpUserList.stream().map(User::getId).collect(Collectors.toList()));
                    }else {
                        log.info("成功!");
                    }
                }, executorService);
                futures.add(voidCompletableFuture);
                upUserList = new ArrayList<>(21);
            }
            try {
                Thread.sleep(RandomUtil.randomInt(1000, 2000));
            } catch (Exception ignored) {
            }
        }
        if (upUserList.size() > 0) {
            boolean batch = userService.updateBatchById(upUserList);
        }
        if (failList.size() > 0) {
            redisCache.setCacheList("upUserAvUrl:fail", failList);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
    }
    public String get(String url, String cookie) {
        HttpResponse request = HttpRequest.get(url)
                .header("content-type", "application/json; charset=UTF-8")
                .header("access-control-allow-origin", "https://wx.zsxq.com")
                .header("origin", "https://wx.zsxq.com")
                .header("date", " Mon, " + new Date().toGMTString())
                .header("expires", " Thu, 19 Nov 1981 08:52:00 GMT")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                .cookie(cookie)
                .execute();
        byte[] bytes = request.bodyBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }
}