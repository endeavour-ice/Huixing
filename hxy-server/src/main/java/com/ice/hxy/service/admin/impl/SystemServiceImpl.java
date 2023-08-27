package com.ice.hxy.service.admin.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.Post;
import com.ice.hxy.mode.entity.PostGroup;
import com.ice.hxy.mode.entity.Tags;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.enums.PostSortedEnum;
import com.ice.hxy.mode.enums.TagCategoryEnum;
import com.ice.hxy.mode.enums.UserStatus;
import com.ice.hxy.mode.request.admin.NoticeReq;
import com.ice.hxy.mode.request.admin.PostCookie;
import com.ice.hxy.mode.request.admin.PostZSReq;
import com.ice.hxy.mode.resp.admin.PostSortedResp;
import com.ice.hxy.mode.resp.tag.TagResp;
import com.ice.hxy.service.PostService.IPostService;
import com.ice.hxy.service.PostService.PostGroupService;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.admin.SystemService;
import com.ice.hxy.service.commService.HttpService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.util.LongUtil;
import com.ice.hxy.util.Threads;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/7/16 10:11
 * @Description: 系统设置服务
 */
@Service
@Slf4j
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
    @Resource
    private IPostService postService;
    @Autowired
    private PostGroupService groupService;

    @Override
    public B<Boolean> upUserAvUrl(String cookie) {
        if (!StringUtils.hasText(cookie)) {
            cookie = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ);
        }
        if (!StringUtils.hasText(cookie)) {
            return B.parameter();
        }
        List<User> list = userService.lambdaQuery().select(User::getId, User::getAvatarUrl).list();
        if (CollectionUtils.isEmpty(list)) {
            return B.ok();
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        String urlZS = "https://api.zsxq.com/v2/users/";
        List<User> userList = new ArrayList<>(1001);
        Random random = new Random();
        for (User user : list) {
            Long id = user.getId();
            String url = urlZS + id;
            label:
            do {
                String body = get(url, cookie);
                JSONObject bo = JSONUtil.parseObj(body);
                String succeeded = bo.getStr("succeeded");
                if (!StringUtils.hasText(succeeded)) {
                    break;
                }
                if (succeeded.equals("true")) {
                    String resp_data = bo.getStr("resp_data");
                    if (!StringUtils.hasText(resp_data)) {
                        break;
                    }
                    JSONObject res = JSONUtil.parseObj(resp_data);
                    String us = res.getStr("user");
                    if (!StringUtils.hasText(us)) {
                        break;
                    }
                    JSONObject ur = JSONUtil.parseObj(us);
                    String avatar_url = ur.getStr("avatar_url");
                    if (!StringUtils.hasText(avatar_url)) {
                        break;
                    }
                    user.setAvatarUrl(avatar_url);
                    userList.add(user);
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException ignored) {
                    }
                    break;
                } else {
                    String code = bo.getStr("code");
                    switch (code) {
                        case "1059":
                            Threads.sleep(random.nextInt(2) + 1);
                            break;
                        case "401":
                        case "10013":
                            break label;
                    }
                }
            } while (true);
            if (userList.size() >= 1000) {
                List<User> finalUserList = userList;
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                    userService.updateBatchById(finalUserList);
                }, executorService);
                futures.add(f);
                userList = new ArrayList<>(1001);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        }
        if (userList.size() > 0) {
            userService.updateBatchById(userList);
        }
        return B.ok();
    }

    public boolean ZSXQURL(String url) {
        Pattern pattern = Pattern.compile("images.zsxq.com");
        Matcher matcher = pattern.matcher(url);
        return matcher.find();
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

    @Override
    public B<Void> upStr() {
        List<Post> list = postService.lambdaQuery().select(Post::getId, Post::getContent).list();
        List<Post> ids = new ArrayList<>(6001);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Post post : list) {
            String str = post.getContent();
            if (!StringUtils.hasText(str)) {
                continue;
            }

            if (ids.size() >= 6000) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                }, executorService);
                postService.updateBatchById(ids);
                ids = new ArrayList<>(6001);
                futures.add(future);
            }
            str = str.replace("%3A", ":");
            str = str.replace("%2F", "/");
            str = str.replace("\n", "<br/>");
            post.setContent(upUrl(str));
            ids.add(post);
        }
        if (ids.size() > 0) {
            postService.updateBatchById(ids);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        return B.ok();
    }

    private String upUrl(String str) {
        String regex = "<e type=\"web\" href=\"(.*?)\" title=\"(.*?)\" />";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String href = matcher.group(1);
            String title = matcher.group(2);
            String aTag = "<a href=\"" + href + "\"" + " title=\"" + title + "\">" + "\uD83D\uDD17链接" + "</a>";
            matcher.appendReplacement(sb, aTag);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public B<Void> pqZS(PostZSReq postZSReq) {
        if (postZSReq == null) {
            return B.empty();
        }
        String cookie = postZSReq.getCookie();
        String groupId = postZSReq.getGroupId();
        String tag = postZSReq.getTag();
        if (!StringUtils.hasText(cookie)) {
            cookie = redisCache.getCacheObject(CacheConstants.ADD_POST_COOKIE_JOB_ZSXQ);
        }
        if (!StringUtils.hasText(cookie) || !StringUtils.hasText(groupId) || !StringUtils.hasText(tag)) {
            return B.empty();
        }
        postZsxq(cookie, groupId, tag);
        return B.ok();
    }

    public void postZsxq(String cookie, String groupsId, String postTag) {
        Set<Long> uids = userService.lambdaQuery().select(User::getId).list().stream().map(User::getId).collect(Collectors.toSet());
        Set<Long> pids = postService.lambdaQuery().select(Post::getId).list().stream().map(Post::getId).collect(Collectors.toSet());
        String url = "https://api.zsxq.com/v2/groups/" + groupsId + "/topics?scope=all&count=20&end_time=2023-08-03T18%3A27%3A13.338%2B0800";
        int i = 0;
        Random random = new Random();
        do {
            String body = getUrl(url, cookie);
            JSONObject bo = JSONUtil.parseObj(body);
            String succeeded = bo.getStr("succeeded");
            if (succeeded.equals("true")) {
                String resp_data = bo.getStr("resp_data");
                JSONObject parseObj = JSONUtil.parseObj(resp_data);
                Object topics = parseObj.get("topics");
                JSONArray objects = JSONUtil.parseArray(topics);
                if (objects.isEmpty()) {
                    if (i == 5) {
                        break;
                    }
                    i++;
                    int L = random.nextInt(5);
                    Threads.sleep(L);
                    continue;
                }
                String parse;
                try {
                    parse = parse(body, cookie, uids, pids, postTag);
                    if (parse == null) {
                        int L = random.nextInt(3) + 2;
                        Threads.sleep(L);
                        continue;
                    }
                    url = "https://api.zsxq.com/v2/groups/" + groupsId + "/topics?scope=all&count=20" + "&end_time=" + URLEncoder.encode(parse, "UTF-8");
                    Thread.sleep(500);
                } catch (Exception e) {
                    log.error("error:{}", e.getMessage());
                    continue;
                }
                i++;
            } else {
                String code = bo.getStr("code");
                switch (code) {
                    case "1059":
                        int L = random.nextInt(5);
                        Threads.sleep(L);
                        continue;
                    case "401":
                    case "10013":
                        L = random.nextInt(3) + 2;
                        Threads.sleep(L);
                }
            }
        } while (true);


    }

    public String parse(String body, String cookie, Set<Long> uids, Set<Long> pids, String postTag) {
        JSONObject parseObj = JSONUtil.parseObj(body);
        Integer code = parseObj.getInt("code");
        if (code != null) {
            return null;
        }
        String resp_data = parseObj.get("resp_data", String.class);
        JSONObject obj = JSONUtil.parseObj(resp_data);
        Object topics = obj.get("topics");
        JSONArray objects = JSONUtil.parseArray(topics);
        Set<User> users = new HashSet<>(objects.size() + 1);
        List<PostGroup> groups = new ArrayList<>();
        Set<Post> posts = new HashSet<>(objects.size() + 1);
        String end_time = null;
        for (int i = 0; i <= objects.size() - 1; i++) {
            Object object = objects.get(i);
            JSONObject jsonObject = JSONUtil.parseObj(object);
            Long topic_id = jsonObject.getLong("topic_id");
            if (i == objects.size() - 1) {
                end_time = jsonObject.getStr("create_time");
            }

            String talk = jsonObject.get("talk", String.class);
            JSONObject entries = JSONUtil.parseObj(talk);
            String text = entries.get("text", String.class);
            String owner = entries.get("owner", String.class);
            JSONObject ow = JSONUtil.parseObj(owner);
            Long user_id = ow.getLong("user_id");
            String name = ow.get("name", String.class);
            String avatar_url = ow.get("avatar_url", String.class);
            String article = entries.getStr("article");
            if (StringUtils.hasText(article)) {
                JSONObject entries1 = JSONUtil.parseObj(article);
                String inline_article_url = entries1.getStr("inline_article_url");
                if (StringUtils.hasText(inline_article_url)) {
                    Document document = Jsoup.parse(getUrl(inline_article_url, cookie));
                    Elements content = document.getElementsByClass("content");
                    text = content.html();
                    text = text.replace("%3A", ":");
                    text = text.replace("%2F", "/");
                    text = text.replace("\n", "<br/>");
                    text = upUrl(text);
                }
            }
            if (!LongUtil.isEmpty(user_id) && !uids.contains(user_id)) {
                User user = new User();
                user.setId(user_id);
                user.setUsername(name);
                user.setUserAccount(name);
                user.setAvatarUrl(avatar_url);
                user.setGender("男");
                user.setPassword("12f1b52ae343c200f385276446a7d1e6");
                user.setUserStatus(UserStatus.NORMAL.getKey());
                users.add(user);
                uids.add(user_id);
            }

            if (!LongUtil.isEmpty(topic_id) && !pids.contains(topic_id) && !LongUtil.isEmpty(user_id)) {
                List<String> tags = new ArrayList<>();
                tags.add(postTag);
                Post post = new Post();
                String digested = jsonObject.getStr("digested");
                if (StringUtils.hasText(digested)) {
                    if (digested.equals("true")) {
                        tags.add("精华");
                    }
                }
                post.setId(topic_id);
                post.setUserId(user_id);
                post.setContent(text);
                post.setTags(GsonUtils.getGson().toJson(tags));
                posts.add(post);
                pids.add(topic_id);
                PostGroup postGroup = new PostGroup();
                postGroup.setPostId(post.getId());
                postGroup.setGroupId(0L);
                groups.add(postGroup);
            }
        }
        if (!CollectionUtils.isEmpty(users)) {
            boolean saveBatch = userService.saveBatch(users);
        }
        if (!CollectionUtils.isEmpty(posts)) {
            boolean batch = postService.saveBatch(posts);
            if (batch) {
                groupService.saveBatch(groups);
            }
        }
        //log.info("post:{} num:{}  user:{} num:{} url:{}", batch, posts.size(), saveBatch, posts.size(), url);
        return end_time;
    }

    public String getUrl(String url, String cookie) {
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
                .cookie(cookie)
                .execute();
        byte[] bytes = request.bodyBytes();
        return new String(bytes, StandardCharsets.UTF_8);

        //return httpService.get(url, httpHeaders, String.class).getBody();

    }
}
