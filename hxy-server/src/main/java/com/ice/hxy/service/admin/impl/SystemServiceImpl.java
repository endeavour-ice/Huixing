package com.ice.hxy.service.admin.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.Tags;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.enums.PostSortedEnum;
import com.ice.hxy.mode.enums.TagCategoryEnum;
import com.ice.hxy.mode.request.admin.NoticeReq;
import com.ice.hxy.mode.request.admin.PostCookie;
import com.ice.hxy.mode.resp.admin.PostSortedResp;
import com.ice.hxy.mode.resp.tag.TagResp;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.admin.SystemService;
import com.ice.hxy.service.commService.HttpService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.util.LongUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/7/16 10:11
 * @Description: 系统设置服务
 */
@Service
public class SystemServiceImpl implements SystemService {
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

    @Override
    public B<Boolean> upUserAvUrl(String cookie) {
        if (!StringUtils.hasText(cookie)) {
            cookie = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ);
            if (!StringUtils.hasText(cookie)) {
                return B.parameter("cookie 为空请先设置");
            }
        }
        List<User> list;
        List<Long> cacheList = redisCache.getCacheList("upUserAvUrl:fail");
        if (cacheList != null && cacheList.size() > 0) {
            list = userService.listByIds(cacheList);
        } else {
            list = userService.lambdaQuery().ge(User::getId, 10L).select(User::getId).list();
        }
        if (list == null || list.size() <= 0) {
            return B.empty();
        }
        List<User> upUserList = new ArrayList<>(101);
        List<Long> failList = new ArrayList<>();
        String url = "https://api.zsxq.com/v2/users/";
        for (User user : list) {
            try {
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
                if (upUserList.size() >= 100) {
                    List<User> finalUpUserList = upUserList;
                    CompletableFuture.runAsync(() -> {
                        boolean batch = userService.updateBatchById(finalUpUserList);
                        if (!batch) {
                            failList.addAll(finalUpUserList.stream().map(User::getId).collect(Collectors.toList()));
                        }
                    }, executorService);
                    upUserList = new ArrayList<>(21);
                }
                try {
                    Thread.sleep(RandomUtil.randomInt(1000, 2000));
                } catch (Exception ignored) {
                }
            } catch (Exception e) {
                failList.addAll(upUserList.stream().map(User::getId).collect(Collectors.toList()));
            }

        }
        if (upUserList.size() <= 0) {
            return B.ok();
        }
        if (failList.size() > 0) {
            redisCache.setCacheList("upUserAvUrl:fail", failList);
        }
        return B.ok(userService.updateBatchById(upUserList));
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

    @Override
    public B<Boolean> postCookie(PostCookie cookie) {
        if (cookie == null) {
            return B.parameter();
        }
        String name = cookie.getName();
        String co = cookie.getCookie();
        if (!StringUtils.hasText(name) || !StringUtils.hasText(co)) {
            return B.parameter();
        }
        switch (name) {
            case "知识星球":
                redisCache.setCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ, co);
                break;
            case "星辰猫":
                redisCache.setCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_XCM, co);
                break;
            case "编程导航":
                redisCache.setCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_bcdh, co);
                break;
            default:
                return B.empty();

        }
        return B.ok(true);
    }

    @Override
    public B<List<PostCookie>> getCookie() {
        String zsxq = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ);
        String xcm = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_XCM);
        String bcdh = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_bcdh);
        List<PostCookie> list = new ArrayList<>();
        list.add(new PostCookie("知识星球", zsxq));
        list.add(new PostCookie("星辰猫", xcm));
        list.add(new PostCookie("编程导航", bcdh));
        return B.ok(list);
    }

    @Override
    public B<Boolean> defaultUserUrl(String av) {
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

    @Override
    public B<Boolean> defaultTeamUrl(String tav) {
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

    @Override
    public B<Boolean> notice(NoticeReq req) {
        if (req == null) {
            return B.parameter();
        }
        String notice = req.getNotice();
        if (StringUtils.hasText(notice)) {
            redisCache.setCacheObject(CacheConstants.NOTICE, notice);
        } else {
            redisCache.deleteObject(CacheConstants.NOTICE);
        }
        return B.ok();
    }

    @Override
    public B<String> getNotice() {
        return B.ok(redisCache.getCacheObject(CacheConstants.NOTICE));
    }

    @Override
    public B<TagResp> getTag() {
        List<Tags> list = tagsService.list();
        if (CollectionUtils.isEmpty(list) || list.get(0).getTagType() == null) {
            return B.empty();
        }
        TagResp tagResp = new TagResp(0L, "root", "0");
        List<TagResp> tagResps = new ArrayList<>();
        Map<Long, List<Tags>> map = list.stream().collect(Collectors.groupingBy(Tags::getCategory));
        int b = 0;
        for (TagCategoryEnum value : TagCategoryEnum.values()) {
            TagResp resp = new TagResp(value.getValue(), value.getDec(), "0-" + b++);
            List<Tags> tags = map.get(value.getValue());
            if (tags != null && !CollectionUtils.isEmpty(tags) && tags.get(0).getTagType() != null) {
                Map<String, List<Tags>> typeMap = tags.stream().collect(Collectors.groupingBy(Tags::getTagType));
                List<TagResp> tagTypeResps = new ArrayList<>();
                int i = 0;
                for (String type : typeMap.keySet()) {
                    TagResp tyr = new TagResp(1L, type, "0-0-" + i++);
                    List<Tags> g = typeMap.get(type);
                    if (!CollectionUtils.isEmpty(g)) {
                        List<TagResp> tagRespList = new ArrayList<>();
                        for (int j = 0; j < g.size(); j++) {
                            tagRespList.add(new TagResp(g.get(j).getId(), g.get(j).getTag(), "0-0-0-" + j));
                        }
                        tyr.setChildren(tagRespList);
                    }
                    tagTypeResps.add(tyr);
                }
                resp.setChildren(tagTypeResps);
            }
            tagResps.add(resp);
        }
        tagResp.setChildren(tagResps);


        return B.ok(tagResp);
    }

    @Override
    public B<Boolean> defaultPostIndex(String value) {
        if (!StringUtils.hasText(value)) {
            return B.empty();
        }
        PostSortedEnum postSortedEnum = PostSortedEnum.hasEnum(value);
        if (postSortedEnum != null) {
            return B.ok(redisCache.setCacheObject(CacheConstants.POST_INDEX_DEFAULT, postSortedEnum.getValue()));
        }
        return B.parameter();
    }

    @Override
    public B<List<PostSortedResp>> getDefaultPostIndex() {
        List<PostSortedResp> resps = new ArrayList<>();
        String va = redisCache.getCacheObject(CacheConstants.POST_INDEX_DEFAULT);
        for (PostSortedEnum value : PostSortedEnum.values()) {
            resps.add(new PostSortedResp(value.getValue(), value.getDec(),
                    StringUtils.hasText(va) && va.equals(value.getValue())));
        }
        return B.ok(resps);
    }
}
