package com.ice.hxy.job;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.*;
import com.ice.hxy.mode.enums.UserStatus;
import com.ice.hxy.service.PostService.IImageService;
import com.ice.hxy.service.PostService.IPostService;
import com.ice.hxy.service.PostService.PostGroupService;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.commService.HttpService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.EmailUtil;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.util.LongUtil;
import com.ice.hxy.util.Threads;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ice
 * @date 2022/8/19 15:15
 */
@Component
@Slf4j
public class ProCacheJob {
    @Resource
    private IImageService iImageService;
    @Resource
    private IUserService userService;
    @Resource
    private IPostService postService;
    @Resource
    private TagsService tagsService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private HttpService httpService;

    @Autowired
    private PostGroupService groupService;

    private static List<Image> imageList = null;
    private static List<String> list = Arrays.asList("Java", "C++", "C", "Python", "PHP", "前端", "GO");

    //每日凌晨启动
    @Scheduled(cron = "0 0 0 * * ?")
    public void AddPostJob() {

        try {
            postJob();
        } catch (Exception ignored) {

        }
        try {
            midjourney();
        } catch (Exception ignored) {

        }
        try {
            yw();
        } catch (Exception ignored) {

        }

    }



    public void postJob() {
        if (!redisCache.hasKey(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ)) {
            log.error("请先添加cookie");
            return;
        }
        String cookie = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ);
        if (!StringUtils.hasText(cookie)) {
            log.error("cookie错误");
            return;
        }
        List<Tags> userTagList = tagsService.list();
        List<String> labelList = userTagList.stream().map(Tags::getTag).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(labelList)) {
            list = labelList;
        }
        try {
            List<Post> postList = new ArrayList<>();
            HashSet<Integer> integers = new HashSet<>();
            do {
                int randomInt = RandomUtil.randomInt(1000, 100000);
                integers.add(randomInt);
            } while (integers.size() != 1000);
            int i = 0;
            ArrayList<Integer> ints = new ArrayList<>(integers);
            HttpResponse request = HttpRequest.get("https://api.zsxq.com/v2/dynamics?scope=general&count=30&begin_time=2022-11-21T16%3A20%3A33.157%2B0800")
                    .header("content-type", "application/json; charset=UTF-8")
                    .header("access-control-allow-origin", "https://wx.zsxq.com")
                    .header("origin", "https://wx.zsxq.com")
                    .header("date", " Mon, " + new Date().toGMTString())
                    .header("expires", " Thu, 19 Nov 1981 08:52:00 GMT")
                    .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                    .cookie(cookie)
                    .execute();
            byte[] bytes = request.bodyBytes();
            String s = new String(bytes, StandardCharsets.UTF_8);
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.select("id");
            List<User> userList = userService.list(userQueryWrapper);
            QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
            postQueryWrapper.select("content");
            List<Post> posts = postService.list(postQueryWrapper);
            Map<String, List<Post>> postMap = posts.stream().collect(Collectors.groupingBy(Post::getContent));
            List<Long> list = userList.stream().map(User::getId).collect(Collectors.toList());
            HashSet<User> users = new HashSet<>();
            JSONObject parseObj = JSONUtil.parseObj(s);
            Object resp_data = parseObj.get("resp_data");
            JSONObject parseObj1 = JSONUtil.parseObj(resp_data);
            Object dynamics = parseObj1.get("dynamics");
            JSONArray objects = JSONUtil.parseArray(dynamics);
            for (Object object : objects) {
                try {
                    JSONObject parseObj2 = JSONUtil.parseObj(object);
                    Object topic = parseObj2.get("topic");
                    JSONObject entries = JSONUtil.parseObj(topic);
                    Object talk = entries.get("talk");
                    JSONObject obj = JSONUtil.parseObj(talk);
                    String article = obj.get("article", String.class);
                    String text;
                    if (StringUtils.hasText(article)) {
                        JSONObject entries1 = JSONUtil.parseObj(article);
                        String inline_article_url = entries1.get("inline_article_url", String.class);
                        if (StringUtils.hasText(inline_article_url)) {
                            HttpResponse res = HttpRequest.get(inline_article_url)
                                    .header("content-type", "application/json; charset=UTF-8")
                                    .header("access-control-allow-origin", "https://wx.zsxq.com")
                                    .header("origin", "https://wx.zsxq.com")
                                    .header("date", " Mon, " + new Date().toGMTString())
                                    .header("expires", " Thu, 19 Nov 1981 08:52:00 GMT")
                                    .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                                    .cookie(cookie)
                                    .execute();
                            Document document = Jsoup.parse(res.body());
                            Elements content = document.getElementsByClass("content");
                            text = content.html();
                        } else {
                            text = (String) obj.get("text");
                        }
                    } else {
                        text = (String) obj.get("text");
                    }

                    Object owner = obj.get("owner");
                    JSONObject parseObjOwner = JSONUtil.parseObj(owner);
                    String userName = (String) parseObjOwner.get("name");
                    String avatar_url = (String) parseObjOwner.get("avatar_url");
                    Long user_id = (Long) parseObjOwner.get("user_id");
                    Set<String> strings = new HashSet<>();
                    for (int j = 0; j < RandomUtil.randomInt(0, ProCacheJob.list.size()); j++) {
                        int randomInt = RandomUtil.randomInt(0, ProCacheJob.list.size());
                        strings.add(ProCacheJob.list.get(randomInt));
                    }
                    if (StringUtils.hasText(user_id.toString()) && StringUtils.hasText(userName) && StringUtils.hasText(avatar_url)) {
                        if (!list.contains(user_id)) {
                            User user = new User();
                            user.setId(user_id);
                            user.setUsername(userName);
                            user.setUserAccount(userName);
                            user.setAvatarUrl(avatar_url);
                            user.setTags(GsonUtils.getGson().toJson(strings));
                            user.setGender("男");
                            user.setPassword("12f1b52ae343c200f385276446a7d1e6");
                            user.setUserStatus(UserStatus.NORMAL.getKey());
                            user.setPlanetCode(ints.get(i++).toString());
                            users.add(user);
                        }

                    }

                    if (StringUtils.hasText(text) && (postMap == null || postMap.get(text) == null || postMap.get(text).size() == 0)) {
                        Post post = new Post();
                        post.setUserId(user_id);
                        post.setContent(text);
                        postList.add(post);
                    }
                } catch (Exception ignored) {
                }
            }
            userService.saveBatch(users);
            postService.saveBatch(postList);
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
                EmailUtil.sendEmail("3521315291@qq.com", "", "定时保存文章失效");
            } catch (Exception ignored) {

            }

        }
    }

    public void addZSXQPost() {
        Random random = new Random();
        if (!redisCache.hasKey(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ)) {
            log.error("请先添加cookie");
            return;
        }
        String cookie = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ);
        if (!StringUtils.hasText(cookie)) {
            log.error("cookie错误");
            return;
        }
        List<Tags> userTagList = tagsService.list();
        List<String> labelList = userTagList.stream().map(Tags::getTag).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(labelList)) {
            list = labelList;
        }
        try {
            String time = null;
            String time2 = "";
            String s = null;
            String url = null;
            for (int j = 0; j <= 100; j++) {
                try {
                    Set<User> users = new HashSet<>();
                    List<Post> postList = new ArrayList<>();
                    HashSet<Integer> integers = new HashSet<>();
                    do {
                        int randomInt = RandomUtil.randomInt(1000, 100000);
                        integers.add(randomInt);
                    } while (integers.size() != 1000);
                    int i = 0;

                    ArrayList<Integer> ints = new ArrayList<>(integers);
                    if (StringUtils.hasText(time)) {

                        String[] split = time.split("\\.");
                        String zzz = split[1];
                        String[] strings = zzz.split("\\+");
                        int t = Integer.parseInt(strings[0]) - 1;
                        if (t < 0) {
                            log.error("错误");
                        }
                        time = split[0] +
                                "." +
                                t +
                                "+" +
                                strings[1];
                        time = URLEncoder.encode(time);
                        url = "https://api.zsxq.com/v2/groups/51122858222824/topics?scope=all&count=20&end_time=" + time;
                    } else {
                        url = "https://api.zsxq.com/v2/groups/51122858222824/topics?scope=all&count=20&end_time=2023-04-23T10%3A41%3A29.761%2B0800";
                    }

                    HttpResponse request = HttpRequest.get(url)
                            .header("content-type", "application/json; charset=UTF-8")
                            .header("access-control-allow-origin", "https://wx.zsxq.com")
                            .header("origin", "https://wx.zsxq.com")
                            .header("host", "api.zsxq.com")
                            .header("accept", "application/json, text/plain, */*")
                            .header("x-request-id", "283659b62-a277-a90f-ffaa-e1ff333875a")
                            .header("x-expire-in", "2591996")
                            .header("x-signature", "9f4d2bd0f722d7890d6eacedc2057d2acf3eba6f-")
                            .header("x-timestamp", String.valueOf(new Date().getTime() / 1000))
                            .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
                            .cookie("sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%22818512224112252%22%2C%22first_id%22%3A%2218372123b234af-0bd731cb9b25278-26021c51-1327104-18372123b24951%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E5%BC%95%E8%8D%90%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC%22%2C%22%24latest_referrer%22%3A%22https%3A%2F%2Fbcdh.yuque.com%2F%22%7D%2C%22identities%22%3A%22eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTgzNzIxMjNiMjM0YWYtMGJkNzMxY2I5YjI1Mjc4LTI2MDIxYzUxLTEzMjcxMDQtMTgzNzIxMjNiMjQ5NTEiLCIkaWRlbnRpdHlfbG9naW5faWQiOiI4MTg1MTIyMjQxMTIyNTIifQ%3D%3D%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%24identity_login_id%22%2C%22value%22%3A%22818512224112252%22%7D%2C%22%24device_id%22%3A%2218372123b234af-0bd731cb9b25278-26021c51-1327104-18372123b24951%22%7D; zsxq_access_token=298566E2-3C3F-8700-71F7-E3AD3731FEBB_3D3D5B55454C1F1C; abtest_env=product; zsxqsessionid=458b507ad5813571c9d4bc5fd879a916")
                            .execute();
                    byte[] bytes = request.bodyBytes();
                    s = new String(bytes, StandardCharsets.UTF_8);
                    QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                    userQueryWrapper.select("id");
                    List<User> userList = userService.list(userQueryWrapper);
                    QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
                    postQueryWrapper.select("content");
                    List<Post> posts = postService.list(postQueryWrapper);
                    Map<String, List<Post>> postMap = posts.stream().collect(Collectors.groupingBy(Post::getContent));
                    Map<Long, List<User>> map = null;
                    if (userList.size() > 0) {
                        map = userList.stream().collect(Collectors.groupingBy(User::getId));
                    }
                    JSONObject parseObj = JSONUtil.parseObj(s);
                    Object resp_data = parseObj.get("resp_data");
                    JSONObject jsonObject = JSONUtil.parseObj(resp_data);
                    Object topics = jsonObject.get("topics");
                    JSONArray objects = JSONUtil.parseArray(topics);

                    Object o = objects.get(objects.size() - 1);
                    JSONObject timeS = JSONUtil.parseObj(o);
                    time = (String) timeS.get("create_time");
                    for (Object object : objects) {
                        try {
                            JSONObject entries = JSONUtil.parseObj(object);
                            Object talk = entries.get("talk");
                            JSONUtil.parseObj(talk);
                            JSONObject obj = JSONUtil.parseObj(talk);
                            String text = (String) obj.get("text");
                            Object owner = obj.get("owner");
                            JSONObject parseObjOwner = JSONUtil.parseObj(owner);
                            String userName = (String) parseObjOwner.get("name");
                            String avatar_url = (String) parseObjOwner.get("avatar_url");
                            Long user_id = (Long) parseObjOwner.get("user_id");
                            Set<String> strings = new HashSet<>();
                            for (int z = 0; z < RandomUtil.randomInt(0, list.size()); z++) {
                                int randomInt = RandomUtil.randomInt(0, list.size());
                                strings.add(list.get(randomInt));
                            }
                            if (StringUtils.hasText(user_id.toString()) && StringUtils.hasText(userName) && StringUtils.hasText(avatar_url) && (map == null || map.get(user_id.toString()) == null || map.get(user_id.toString()).size() == 0)) {
                                User user = new User();
                                user.setId(user_id);
                                user.setUsername(userName);
                                user.setUserAccount(userName);
                                user.setAvatarUrl(avatar_url);
                                user.setTags(GsonUtils.getGson().toJson(strings));
                                user.setGender("男");
                                user.setPassword("12f1b52ae343c200f385276446a7d1e6");
                                user.setUserStatus(UserStatus.NORMAL.getKey());
                                user.setPlanetCode(ints.get(i++).toString());
                                users.add(user);
                            }

                            if (StringUtils.hasText(text) && (postMap == null || postMap.get(text) == null || postMap.get(text).size() == 0)) {
                                Post post = new Post();
                                post.setUserId(user_id);
                                post.setContent(text);
                                postList.add(post);
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    userService.saveBatch(users);
                    postService.saveBatch(postList);
                    System.out.println("================还剩下" + (99 - j) + "次================");
                    int aLong = random.nextInt(8);
                    aLong += 2;
                    try {
                        Thread.sleep(aLong * 1000);
                    } catch (InterruptedException ignored) {
                        log.error("暂停错误");
                    }
                } catch (Exception e) {
                    log.error(url);
                    log.error(s);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

        }


    }

    void Test() {
        String url = "https://api.zsxq.com/v2/groups/51122858222824/topics?scope=all&count=20";
        HttpResponse request = HttpRequest.get(url)
                .header("content-type", "application/json; charset=UTF-8")
                .header("access-control-allow-origin", "https://wx.zsxq.com")
                .header("origin", "https://wx.zsxq.com")
                .header("x-expire-in", "2591996")
                .header("x-timestamp", String.valueOf(new Date().getTime()))
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                .cookie("sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%22818512224112252%22%2C%22first_id%22%3A%2218372123b234af-0bd731cb9b25278-26021c51-1327104-18372123b24951%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E5%BC%95%E8%8D%90%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC%22%2C%22%24latest_referrer%22%3A%22https%3A%2F%2Fbcdh.yuque.com%2F%22%7D%2C%22identities%22%3A%22eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTgzNzIxMjNiMjM0YWYtMGJkNzMxY2I5YjI1Mjc4LTI2MDIxYzUxLTEzMjcxMDQtMTgzNzIxMjNiMjQ5NTEiLCIkaWRlbnRpdHlfbG9naW5faWQiOiI4MTg1MTIyMjQxMTIyNTIifQ%3D%3D%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%24identity_login_id%22%2C%22value%22%3A%22818512224112252%22%7D%2C%22%24device_id%22%3A%2218372123b234af-0bd731cb9b25278-26021c51-1327104-18372123b24951%22%7D; zsxq_access_token=298566E2-3C3F-8700-71F7-E3AD3731FEBB_3D3D5B55454C1F1C; abtest_env=product; zsxqsessionid=458b507ad5813571c9d4bc5fd879a916")
                .execute();

        String body = request.body();
        System.out.println(body);
        JSONObject parseObj = JSONUtil.parseObj(body);
        Object resp_data = parseObj.get("resp_data");
        JSONObject jsonObject = JSONUtil.parseObj(resp_data);
        Object topics = jsonObject.get("topics");
        JSONArray objects = JSONUtil.parseArray(topics);
        String time = objects.get(19, String.class);
        for (Object object : objects) {
            JSONObject entries = JSONUtil.parseObj(object);
            Object talk = entries.get("talk");
            Long postId = (Long) entries.get("topic_id");
            JSONUtil.parseObj(talk);
            JSONObject obj = JSONUtil.parseObj(talk);
            String content = (String) obj.get("text");
            Object owner = obj.get("owner");
            JSONObject parseObjOwner = JSONUtil.parseObj(owner);
            String userName = (String) parseObjOwner.get("name");
            String avatar_url = (String) parseObjOwner.get("avatar_url");
            Long user_id = (Long) parseObjOwner.get("user_id");
            System.out.println(userName);
        }
    }

    public void midjourney() {
        try {
            imageList = iImageService.list();
            Map<String, Image> collect = null;
            if (list.size() > 0) {
                collect = imageList.stream().collect(Collectors.toMap(Image::getImageUrl, i -> i));
            }
            String url = "https://www.midjourney.com/_next/data/GTwZLlxzdYTCQQCUeZkqi/showcase/recent.json";
            String resp = HttpRequest.get(url)
                    .timeout(2000000)
                    .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
                    .header("cookie", "__Host-next-auth.csrf-token=d59c6207c68c61eadb0f4b88ffbb02683d22ea18c5924718a00752f44aadf50c%7Cdaae57398ac1081b8ea3cb3ccc8dff24a5822d2b728efdd500b035e81686e99c; imageSize=medium; imageLayout_2=hover; getImageAspect=2; fullWidth=false; showHoverIcons=true; __stripe_mid=c5b480e4-1695-47b9-83ac-32afeb5ef97b707e96; cf_chl_2=f271ef0cb6c741a; cf_clearance=hqB4X9aulECGX828Ec6H_vje4b1JXxb1HuteU.lahjo-1683271824-0-160; __Secure-next-auth.callback-url=https%3A%2F%2Fwww.midjourney.com%2Faccount%2F; __stripe_sid=027cd701-84a7-435e-92da-db143b64adeacc98fd; _dd_s=rum=0&expire=1683276104965")
                    .execute().body();
            JSONObject parseObj = JSONUtil.parseObj(resp);
            Object pageProps = parseObj.get("pageProps");
            if (pageProps == null) {
                return;
            }
            JSONObject object = JSONUtil.parseObj(pageProps);
            Object jobs = object.get("jobs");
            if (jobs == null) {
                return;
            }
            JSONArray jsonArray = JSONUtil.parseArray(jobs);
            if (jsonArray.size() <= 0) {
                return;
            }
            ArrayList<Image> imageArrayList = new ArrayList<>();
            for (Object o : jsonArray) {
                JSONObject entries = JSONUtil.parseObj(o);
                Object image_paths = entries.get("image_paths");
                if (image_paths == null) {
                    continue;
                }
                JSONArray objects = JSONUtil.parseArray(image_paths);
                String image_url = (String) objects.get(0);
                if (collect != null) {
                    Image image = collect.get(image_url);
                    if (image != null) {
                        continue;
                    }
                }
                Image image = new Image();
                image.setImageUrl(image_url);
                imageArrayList.add(image);
            }
            iImageService.saveBatch(imageArrayList);
        } catch (Exception e) {

        }

    }

    public void yw() {
        try {
            if (imageList == null) {
                imageList = iImageService.list();
            }
            Map<String, Image> collect = null;
            if (list.size() > 0) {
                collect = imageList.stream().collect(Collectors.toMap(Image::getImageUrl, i -> i));
            }
            String url = "https://mars-prod.whalean.com/poseidon-service/api/wp/wallpaper/hot/class/0/list?pageNo=2&pageSize=12";
            HttpResponse request = HttpRequest.get(url)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "okhttp/4.9.1")
                    .header("Host", "mars-prod.whalean.com")
                    .header("appkey", "mobile")
                    .header("Connection", "Keep-Alive")
                    .header("Accept-Encoding", "gzip")
                    .header("appkey", "mobile")
                    .header("user_id", "38152252")
                    .header("userId", "38152252")
                    .header("extendedFields", " %7B%22%24screen_orientation%22%3A%22%22%2C%22%24carrier%22%3A%22%E4%B8%AD%E5%9B%BD%E7%A7%BB%E5%8A%A8%22%2C%22%24bot_name%22%3A%22%22%2C%22%24os_version%22%3A%2213%22%2C%22%24is_first_day%22%3Afalse%2C%22ab_param%22%3A%22%22%2C%22%24model%22%3A%2222041216C%22%2C%22%24os%22%3A%22android%22%2C%22%24longitude%22%3A%22%22%2C%22%24screen_width%22%3A1080%2C%22project%22%3A%22yaowang%22%2C%22%24wifi%22%3A%22false%22%2C%22%24network_type%22%3A%22UN_KNOWN%22%2C%22%24screen_height%22%3A2316%2C%22%24app_version%22%3A%225.2.1%22%2C%22%24device_id%22%3A%2242b203e68bb9ea25%22%2C%22platform_type%22%3A%22%E5%AE%89%E5%8D%93%22%2C%22distinct_id%22%3A38152252%2C%22appId%22%3A%221000%22%2C%22%24url%22%3A%22%22%2C%22%24latitude%22%3A%22%22%2C%22%24manufacturer%22%3A%22Xiaomi%22%7D")
                    .header("token", "PNGY9x0OLL1865gQxZT6yg==")
                    .header("Authorization", "Bearer PNGY9x0OLL1865gQxZT6yg==")
                    .execute();
            byte[] bytes = request.bodyBytes();
            String resp = new String(bytes, StandardCharsets.UTF_8);
            JSONObject respObject = JSONUtil.parseObj(resp);
            Object data = respObject.get("data");
            JSONObject parseObj = JSONUtil.parseObj(data);
            Object list = parseObj.get("list");
            JSONArray parseArray = JSONUtil.parseArray(list);
            Set<String> ImageList = new HashSet<>();
            for (Object a : parseArray) {
                JSONObject obj = JSONUtil.parseObj(a);
                String image_url = (String) obj.get("url");

                if (StringUtils.hasText(image_url)) {
                    if (collect != null) {
                        if (collect.get(image_url) == null) {
                            ImageList.add(image_url);
                        }
                    } else {
                        ImageList.add(image_url);
                    }

                }
                Object imageList = obj.get("image");

                JSONArray image = JSONUtil.parseArray(imageList);

                for (Object o : image) {
                    JSONObject oUrl = JSONUtil.parseObj(o);
                    String tUrl = (String) oUrl.get("url");
                    if (StringUtils.hasText(tUrl)) {
                        if (collect != null) {
                            if (collect.get(tUrl) == null) {
                                ImageList.add(tUrl);
                            }
                        } else {
                            ImageList.add(tUrl);
                        }
                    }
                }

            }
            if (ImageList.size() > 0) {
                List<Image> images = ImageList.stream().map(s -> {
                    Image image = new Image();
                    image.setImageUrl(s);
                    return image;
                }).collect(Collectors.toList());
                iImageService.saveBatch(images);
            }

        } catch (Exception e) {

        }

    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("page", 1);
        paramMap.put("type", "all");
        paramMap.put("load_type", "ajax");
        paramMap.put("index", 0);
        String s = HttpUtil.get("https://www.xcng.cn/wp-content/module/public/gadget/LS-SYZQ-S/data.php", paramMap);
        System.out.println(s);
        Document document = Jsoup.parse(s);
    }

}