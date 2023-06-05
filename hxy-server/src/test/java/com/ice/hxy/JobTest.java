package com.ice.hxy;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ice.hxy.job.ProCacheJob;
import com.ice.hxy.mode.entity.Post;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.enums.UserStatus;
import com.ice.hxy.service.PostService.IImageService;
import com.ice.hxy.service.PostService.IPostService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.commService.HttpService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.util.LongUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/5/26 9:04
 * @Description: TODO
 */
@SpringBootTest
public class JobTest {
    @Resource
    private HttpService httpService;
    @Resource
    private IImageService iImageService;
    @Resource
    private IUserService userService;
    @Resource
    private IPostService postService;
    @Resource
    private ProCacheJob proCacheJob;
    @Resource
    private RedisCache redisCache;
    @Test
    void JobTestAAA() {
        proCacheJob.postZsxq();

    }

    @Test
    void save() {
        List<String> list = Arrays.asList("Java", "C++", "C", "Python", "PHP", "前端", "GO");
        Set<User> users = new HashSet<>();
        List<Post> postList = new ArrayList<>();
        HashSet<Integer> integers = new HashSet<>();
        do {
            int randomInt = RandomUtil.randomInt(1000, 100000);
            integers.add(randomInt);
        } while (integers.size() != 1000);
        int i = 0;
        ArrayList<Integer> ints = new ArrayList<>(integers);
        String url = "https://api.zsxq.com/v2/dynamics?scope=general&count=30&begin_time=2022-11-21T16%3A20%3A33.157%2B0800";
        String body = get(url);
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
        JSONObject parseObj = JSONUtil.parseObj(body);
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

                        Document document = Jsoup.parse(get(inline_article_url));
                        Elements content = document.getElementsByClass("content");
                        text = content.html();
                    }else {
                        text = (String) obj.get("text");
                    }
                }else {
                    text = (String) obj.get("text");
                }

                Object owner = obj.get("owner");
                JSONObject parseObjOwner = JSONUtil.parseObj(owner);
                String userName = (String) parseObjOwner.get("name");
                String avatar_url = (String) parseObjOwner.get("avatar_url");
                Long user_id = (Long) parseObjOwner.get("user_id");
                Set<String> strings = new HashSet<>();
                for (int j = 0; j < RandomUtil.randomInt(0, list.size()); j++) {
                    int randomInt = RandomUtil.randomInt(0, list.size());
                    strings.add(list.get(randomInt));
                }
                if (!LongUtil.isEmpty(user_id) && StringUtils.hasText(userName) && StringUtils.hasText(avatar_url) && (map == null || map.get(user_id.toString()) == null || map.get(user_id.toString()).size() == 0)) {
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

                if (!LongUtil.isEmpty(user_id) && (postMap == null || postMap.get(text) == null || postMap.get(text).size() == 0)) {
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
    }

    public String get(String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("content-type", "application/json; charset=UTF-8");
        httpHeaders.add("access-control-allow-origin", "https://wx.zsxq.com");
        httpHeaders.add("origin", "https://wx.zsxq.com");
        httpHeaders.add("date", " Mon, " + new Date().toGMTString());
        httpHeaders.add("expires", " Thu, 19 Nov 1981 08:52:00 GMT");
        httpHeaders.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
        httpHeaders.add("Cookie", "zsxq_access_token=FB4DDE37-4083-993C-6771-41B5D413FF62_3D3D5B55454C1F1C; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%22818512224112252%22%2C%22first_id%22%3A%221881e2a58cd9e-0a22e7e8f6a8b3-26031a51-1327104-1881e2a58ce87f%22%2C%22props%22%3A%7B%7D%2C%22identities%22%3A%22eyIkaWRlbnRpdHlfY29va2llX2lkIjoiMTg4MWUyYTU4Y2Q5ZS0wYTIyZTdlOGY2YThiMy0yNjAzMWE1MS0xMzI3MTA0LTE4ODFlMmE1OGNlODdmIiwiJGlkZW50aXR5X2xvZ2luX2lkIjoiODE4NTEyMjI0MTEyMjUyIn0%3D%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%24identity_login_id%22%2C%22value%22%3A%22818512224112252%22%7D%2C%22%24device_id%22%3A%221881e2a58cd9e-0a22e7e8f6a8b3-26031a51-1327104-1881e2a58ce87f%22%7D; abtest_env=product; zsxqsessionid=dea08f543d01e046b248cef61e47cbc2");
        return httpService.get(url, httpHeaders, String.class).getBody();
    }
}
