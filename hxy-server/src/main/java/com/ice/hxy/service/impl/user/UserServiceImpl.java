package com.ice.hxy.service.impl.user;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.config.initConfig.ConstantProperties;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mapper.UserMapper;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.constant.LoginType;
import com.ice.hxy.mode.dto.PageFilter;
import com.ice.hxy.mode.dto.QQInfo;
import com.ice.hxy.mode.dto.QQLogin;
import com.ice.hxy.mode.entity.Tags;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.entity.vo.UserAvatarVo;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.enums.UserStatus;
import com.ice.hxy.mode.request.*;
import com.ice.hxy.mode.resp.SafetyUserResponse;
import com.ice.hxy.mq.RabbitService;
import com.ice.hxy.service.TagService.TagsService;
import com.ice.hxy.service.UserService.IUserService;
import com.ice.hxy.service.commService.HttpService;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.service.commService.TokenService;
import com.ice.hxy.util.*;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ice.hxy.mode.constant.UserConstant.ASSISTANT_ID;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author ice
 * @since 2022-06-14
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private RedisCache redisCache;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private TagsService tagsService;
    @Resource
    private ExecutorService executorService;
    @Resource
    private TokenService tokenService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private HttpService httpService;
    @Override
    public B<SafetyUserResponse> getCurrent(HttpServletRequest request) {
        User user = tokenService.getTokenUser(request);
        return user == null ? B.login() : B.ok(UserUtils.getSafetyUserResponse(user));
    }

    @Override
    public B<UserVo> getAdminCurrent(HttpServletRequest request) {
        User currentUser = tokenService.getTokenUser(request);
        if (!UserRole.isAdmin(currentUser)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        return B.ok(UserUtils.getSafetyUser(currentUser));
    }

    @Override
    public B<String> userRegister(UserRegisterRequest userRegister) {
        RLock lock = redissonClient.getLock(CacheConstants.REGISTER);
        try {
            if (lock.tryLock(0, 3000, TimeUnit.MILLISECONDS)) {
                if (userRegister == null) {
                    return B.parameter();
                }
                return register(userRegister);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        } finally {
            lock.unlock();
        }
        return B.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    public B<String> register(UserRegisterRequest userRegister) {
        String userAccount = userRegister.getUserAccount();
        String password = userRegister.getPassword();
        String checkPassword = userRegister.getCheckPassword();
        String planetCode = userRegister.getPlanetCode();
        String code = userRegister.getCode();
        String email = userRegister.getEmail();
        if (!StringUtils.hasText(planetCode)) {
            planetCode = RandomUtil.randomInt(10, 10000) + "";
        }
        boolean hasEmpty = StrUtil.hasEmpty(userAccount, password, checkPassword, planetCode);
        if (hasEmpty) {
            return B.parameter();
        }
        // 1. 校验
        if (StrUtil.hasEmpty(userAccount, password, checkPassword, planetCode)) {
            return B.parameter();
        }
        if (userAccount.length() < 3) {
            return B.parameter("用户名过短");
        }

        if (password.length() < 6 || checkPassword.length() < 6) {
            return B.parameter("密码过短");
        }
        if (planetCode.length() > 5) {
            return B.parameter("编号过长");
        }
        // 校验账户不能包含特殊字符

        if (REUtils.isName(userAccount)) {
            return B.parameter("账号特殊符号");
        }
        // 判断密码和和用户名是否相同
        if (password.equals(userAccount)) {
            return B.parameter("账号密码相同");
        }
        if (!password.equals(checkPassword)) {
            return B.parameter("确认密码错误");

        }

        if (!StringUtils.hasText(email)) {
            return B.parameter("邮箱为空");
        }
        email = email.toLowerCase();
        if (REUtils.isEmail(email)) {
            return B.parameter("请输入正确邮箱");
        }
        if (!StringUtils.hasText(code)) {
            return B.parameter("验证码为空");
        }
        String redisCode = redisCache.getCacheObject(CacheConstants.REDIS_REGISTER_CODE + email);

        if (!StringUtils.hasText(redisCode) || !code.equals(redisCode)) {
            return B.parameter("验证码错误,请重试");
        }
        // 判断用户是否重复
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_account", userAccount);
        Long aLong = baseMapper.selectCount(wrapper);
        if (aLong > 0) {
            return B.parameter("注册用户重复");
        }
        // 判断用户是否重复
        wrapper = new QueryWrapper<>();
        wrapper.eq("planet_code", planetCode);
        Long a = baseMapper.selectCount(wrapper);
        if (a > 0) {
            return B.parameter("注册用户重复");
        }
        wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            return B.parameter("注册邮箱重复");
        }
        // 加密密码
        String passwordMD5 = MD5.getMD5(password);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(passwordMD5);
        user.setPlanetCode(planetCode);
        user.setAvatarUrl(AvatarUrlUtils.getRandomUrl());
        if (redisCache.hasKey(CacheConstants.DEFAULT_AVATAR)) {
            List<String> cacheList = redisCache.getCacheList(CacheConstants.DEFAULT_AVATAR);
            String randomUrl = AvatarUrlUtils.getRandomUrl(cacheList);
            if (StringUtils.hasText(randomUrl)) {
                user.setAvatarUrl(randomUrl);
            }
        }
        user.setLoginType(LoginType.EMAIL);
        user.setUsername(userAccount);
        user.setEmail(email);
        boolean save = this.save(user);

        if (!save) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册用户失败");
        }
        Long saveId = user.getId();
        if (LongUtil.isEmpty(saveId)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册用户失败");
        }
        long id = IdUtil.getSnowflakeNextId();
        int is = baseMapper.saveFriend(String.valueOf(id), saveId, ASSISTANT_ID);
        if (is != 1) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "注册用户失败");
        }

        return B.ok(tokenService.setToken(user));
    }

    @Override
    public B<String> userLogin(UserLoginRequest userLogin, HttpServletRequest request) {
        if (userLogin == null) {
            return B.parameter();
        }
        String code = userLogin.getCode();
        String uuid = userLogin.getUuid();
        String userAccount = userLogin.getUserAccount();
        String password = userLogin.getPassword();
        if (!StringUtils.hasText(uuid)) {
            return B.error(ErrorCode.ERROR_CODE);

        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String codeImage = redisCache.getCacheObject(verifyKey);
        if (!StringUtils.hasText(codeImage)) {
            return B.error(ErrorCode.ERROR_CODE);

        }
        if (!code.equals(codeImage)) {
            return B.error(ErrorCode.ERROR_CODE);
        }
        return login(userAccount, password, false);
    }

    public B<String> login(String userAccount, String password, boolean isAdmin) {
        if (!StringUtils.hasText(userAccount) || !StringUtils.hasText(userAccount)) {
            return B.parameter("账号密码错误");
        }
        // 1. 校验
        if (userAccount.length() <= 0) {
            return B.parameter("账号密码错误");
        }
        if (password.length() < 6) {
            return B.parameter("账号密码错误");
        }
        // 校验账户不能包含特殊字符
        if (REUtils.isName(userAccount)) {
            return B.parameter("账号密码错误");
        }

        String passwordMD5 = MD5.getMD5(password);

        User user = baseMapper.selectByLogin(userAccount, passwordMD5);

        if (user == null) {
            return B.parameter("账号密码错误");
        }

        // 用户脱敏
        if (user.getUserStatus().equals(UserStatus.LOCKING.getKey())) {
            return B.parameter("以封号，请联系管理员");
        }
        if (isAdmin && !UserRole.isAdmin(user)) {
            return B.auth();
        }
        return B.ok(tokenService.setToken(user));

    }

    @Override
    public B<String> userAdminLogin(UserLoginRequest userLogin) {
        if (userLogin == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "数据为空!");
        }
        String userAccount = userLogin.getUserAccount();
        String password = userLogin.getPassword();
        return login(userAccount, password, true);
    }


    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public B<Void> userLogout(HttpServletRequest request) {
        User user = UserUtils.getLoginUser();
        tokenService.remove(request, user);
        Threads.time().execute(new TimerTask() {
            @Override
            public void run() {
                tokenService.remove(request, user);
            }
        });
        return B.ok();
    }

    /**
     * 修改用户
     *
     * @param user
     * @return
     */
    @Override
    public B<Integer> updateUser(User user) {
        if (user == null) {
            return B.parameter();
        }
        boolean admin = UserRole.isAdmin();
        if (!admin) {
            return B.auth();
        }
        int update = baseMapper.updateById(user);
        if (update <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "修改失败");
        }
        return B.ok(update);
    }


    /**
     * ===============================================================
     * 根据标签搜索用户
     *
     * @return 返回用户列表
     */
    @Override
    public B<List<SafetyUserResponse>> searchUserTag(UserSearchTagAndTxtRequest userSearchTagAndTxtRequest) {
        if (userSearchTagAndTxtRequest == null) {
            return B.parameter();
        }

        List<String> tagNameList = userSearchTagAndTxtRequest.getTagNameList();
        String searchTxt = userSearchTagAndTxtRequest.getSearchTxt();
        if (!StringUtils.hasText(searchTxt) && CollectionUtils.isEmpty(tagNameList)) {
            return B.parameter();
        }
        // sql 语句查询
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        // 拼接and 查询
//        for (String tagName : tagNameList) {
//            wrapper = wrapper.like("tags", tagName);
//        }
//        List<User> userList = baseMapper.selectList(wrapper);
        // 内存查询
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(searchTxt)) {
            wrapper.and(wq -> wq.like("username", searchTxt).or().like("user_account", searchTxt)
                    .or().like("gender", searchTxt).or().like("tel", searchTxt).or().like("email", searchTxt).
                    like("profile", searchTxt));
        }
        List<User> userList = baseMapper.selectList(wrapper);
        if (userList.size() <= 0) {
            return B.empty();
        }
        List<SafetyUserResponse> safetyUserResponses;
        if (!CollectionUtils.isEmpty(tagNameList)) {
            Gson gson = GsonUtils.getGson();
            safetyUserResponses = userList.stream().filter(user -> {
                String tagStr = user.getTags();
                // 将json 数据解析成 Set
                Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
                }.getType());
                tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
                for (String tagName : tagNameList) {
                    if (tempTagNameSet.contains(tagName)) {
                        return true;
                    }
                }
                return false;
            }).map(UserUtils::getSafetyUserResponse).collect(Collectors.toList());

        } else {
            safetyUserResponses = userList.stream().map(UserUtils::getSafetyUserResponse).collect(Collectors.toList());
        }

        return B.ok(safetyUserResponses);
    }


    @Override
    public B<Map<String, Object>> selectPageIndexList(long current, long size) {
        PageFilter filter = new PageFilter(current, size);
        current = filter.getCurrent();
        size = filter.getSize();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_status", UserStatus.NORMAL.getKey());
        long count = this.count(wrapper);
        List<SafetyUserResponse> userVoList = baseMapper.selectUserVoList(current, size, UserStatus.NORMAL.getKey());
        HashMap<String, Object> map = new HashMap<>();
        map.put("items", userVoList);
        map.put("total", count);
        return B.ok(map);
    }

    /**
     * 根据用户修改资料
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public B<String> getUserByUpdateID(UpdateUserRequest updateUsers) {
        if (updateUsers == null) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        Long userId = updateUsers.getId();
        String username = updateUsers.getUsername();
        String gender = updateUsers.getGender();
        String tel = updateUsers.getTel();
        String email = updateUsers.getEmail();
        String profile = updateUsers.getProfile();
        String tags = updateUsers.getTags();
        String status = updateUsers.getStatus();
        if (StringUtils.hasText(status) && UserStatus.PRIVATE.getName().equals(status)) {
            loginUser.setUserStatus(UserStatus.PRIVATE.getKey());
        }
        if (StringUtils.hasText(status) && "公开".equals(status)) {
            loginUser.setUserStatus(UserStatus.NORMAL.getKey());
        }
        if (!StringUtils.hasText(username) && !StringUtils.hasText(tel) &&
                !StringUtils.hasText(email) && !StringUtils.hasText(tags)
                && !StringUtils.hasText(gender) && !StringUtils.hasText(profile) && !StringUtils.hasText(status)) {
            return B.parameter();
        }
        if (StringUtils.hasText(username)) {
            // 将特殊字符替换为空字符串
            String regEx = "\\pP|\\pS|\\s+";
            username = Pattern.compile(regEx).matcher(username).replaceAll("").trim();
            loginUser.setUsername(username);
        }
        if (LongUtil.isEmpty(userId)) {
            return B.parameter();
        }
        if (StringUtils.hasText(profile)) {
            if (profile.length() >= 200) {
                return B.parameter();
            } else if (profile.equals(loginUser.getProfile())) {
                return B.parameter("重复设置");
            }
            loginUser.setProfile(profile);
        }
        if (StringUtils.hasText(gender)) {
            if (!"男".equals(gender) && !"女".equals(gender)) {
                return B.parameter();
            } else if (gender.equals(loginUser.getGender())) {
                return B.parameter("重复设置");

            }
            loginUser.setGender(gender);
        }
        if (!UserRole.isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            return B.auth();
        }
        User oldUser = baseMapper.selectById(userId);
        if (oldUser == null) {
            return B.parameter();
        }
        if (StringUtils.hasText(tel)) {
            if (tel.equals(loginUser.getTel())) {
                return B.parameter("重复设置");
            }
            if (REUtils.isTel(tel)) {
                return B.parameter("手机号格式错误");

            }
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("tel", tel);
            Long count = baseMapper.selectCount(wrapper);
            if (count != null && count > 0) {
                return B.parameter("手机号已被注册");

            }
            loginUser.setTel(tel);
        }
        if (StringUtils.hasText(email)) {
            email = email.toLowerCase();
            if (this.seeUserEmail(email)) {
                return B.parameter("该邮箱已被注册");
            }
            String updateUsersCode = updateUsers.getCode();
            if (!StringUtils.hasText(updateUsersCode)) {
                return B.parameter("验证码错误");
            }
            String redisKey = CacheConstants.REDIS_FILE_BY_BING_DING_KEY + email;
            String code = redisCache.getCacheObject(redisKey);
            if (!StringUtils.hasText(code)) {
                return B.parameter("验证码错误");
            }
            if (!updateUsersCode.equals(code)) {
                return B.parameter("验证码错误");
            }
        }
        // 设置标签
        if (StringUtils.hasText(tags)) {
            try {
                Gson gson = GsonUtils.getGson();
                List<Long> tag = gson.fromJson(tags, new TypeToken<List<Long>>() {
                }.getType());
                List<Tags> tagList = tagsService.listByIds(tag);
                if (tagList.isEmpty()) {
                    return B.parameter();
                }
                List<String> labels = tagList.stream().map(Tags::getTag).collect(Collectors.toList());
                tags = gson.toJson(labels);
                if (tags.equals(loginUser.getTags())) {
                    return B.parameter("标签重复设置");
                }
                boolean isBoolean = this.TagsUtil(userId);
                if (isBoolean) {
                    loginUser.setTags(tags);
                } else {
                    return B.parameter();
                }
            } catch (Exception e) {
                log.error("getUserByUpdateID 设置标签 error:{}", e.getMessage());
                return B.parameter();
            }
        }
        int update = baseMapper.updateById(loginUser);
        if (update > 0) {
            rabbitService.sendDelRedisKey(CacheConstants.REDIS_INDEX_KEY);
        } else {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "修改失败,请重试");
        }
        String token = tokenService.setToken(loginUser);
        return B.ok(token);

    }


    @Override
    public B<Page<SafetyUserResponse>> friendUserName(UserSearchPage userSearchPage) {
        User user = UserUtils.getLoginUser();
        if (userSearchPage == null) {
            return B.parameter();
        }

        String userName = userSearchPage.getUserName();
        if (!StringUtils.hasText(userName)) {
            return B.parameter("请输入账号");
        }
        Long id = user.getId();

        Page<User> page = new Page<>(userSearchPage.getCurrent(), userSearchPage.getSize());
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("user_account", userName).or().like("username", userName);
        Page<User> userPage = this.page(page, wrapper);
        List<User> userList = userPage.getRecords();
        List<User> voList = userList.stream()
                .filter(userVo -> !userVo.getId().equals(id) && userVo.getUserStatus() != UserStatus.PRIVATE.getKey())
                .collect(Collectors.toList());
        userPage.setRecords(voList);
        return B.ok(UserUtils.getPageVo(userPage));
    }


    public boolean TagsUtil(Long userId) {
        String tagKey = CacheConstants.TAG_REDIS_KEY + userId;
        Integer tagNum = redisCache.getCacheObject(tagKey);
        if (tagNum == null) {
            return redisCache.setCacheObject(tagKey, 1,
                    DateUtils.getRemainSecondsOneDay(), TimeUnit.SECONDS);
        }
        if (tagNum > 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "今天修改次数以上限...");
        }
        try {
            redisCache.increment(tagKey);
        } catch (Exception e) {
            return false;
        }
        rabbitService.sendDelRedisKey(CacheConstants.REDIS_INDEX_KEY);
        return true;
    }


    /**
     * 根据名字查找
     *
     * @param current
     * @param size
     * @param userAccount
     * @return
     */
    private List<SafetyUserResponse> getSearchUserByPage(Long current, Long size, String userAccount) {
        return baseMapper.selectFindByUserAccountLikePage(current, size, userAccount);
    }

    /**
     * 搜索用户的标签
     *
     * @param tag     标签
     * @param request 登录的请求
     * @return 返回标签
     */
    @Override
    public B<List<SafetyUserResponse>> searchUserTag(String tag, HttpServletRequest request) {
        if (!StringUtils.hasText(tag)) {
            return B.parameter();
        }
        UserUtils.getLoginUser();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("tags", tag);
        Page<User> commentPage = baseMapper.selectPage(new Page<>(1, 200), wrapper);
        List<User> list = commentPage.getRecords();
        return B.ok(list.parallelStream().map(UserUtils::getSafetyUserResponse).collect(Collectors.toList()));
    }

    /**
     * 通过编辑距离算法 推荐用户
     *
     * @param num 推荐的数量
     * @return 返回
     */
    @Override
    public B<List<SafetyUserResponse>> matchUsers(long num) {
        if (num <= 0 || num > 20) {
            return B.parameter();
        }
        User loginUser = UserUtils.getLoginUser();
        String tags = loginUser.getTags();
        if (!StringUtils.hasText(tags)) {
            return B.parameter("请先设置标签");
        }
        Gson gson = GsonUtils.getGson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
//        SortedMap<Integer, User> indexDistanceMap = new TreeMap<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "tags");
        wrapper.isNotNull("tags");
        List<User> userList = this.list(wrapper);
        List<Pair<Integer, Long>> pairs = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            if (!StringUtils.hasText(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> tagUserList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            int distance = AlgorithmUtils.minDistance(tagList, tagUserList);
            int size = pairs.size();
            if (size - 1 >= num) {
                pairs.sort(Comparator.comparingInt(Pair::getKey));
                Pair<Integer, Long> pair = pairs.get(size - 1);
                Integer key = pair.getKey();
                if (distance >= key) {
                    continue;
                }
                pairs.set(size - 1, new Pair<>(distance, user.getId()));

            } else {
                pairs.add(new Pair<>(distance, user.getId()));
            }
        }
        List<SafetyUserResponse> findUserList = new ArrayList<>();
        if (pairs.size() > 0) {
            List<Long> userIds = pairs.stream().map(Pair::getValue).collect(Collectors.toList());
            List<User> users = this.listByIds(userIds);
            if (users == null || users.size() <= 0) {
                return B.ok(findUserList);
            }
            // 用户id进行分组
            Map<Long, List<SafetyUserResponse>> userListByUserIdMap = users.stream().map(UserUtils::getSafetyUserResponse).collect(Collectors.groupingBy(SafetyUserResponse::getId));

            for (Long userId : userIds) {
                findUserList.add(userListByUserIdMap.get(userId).get(0));
            }
        }
        return B.ok(findUserList);
//        return indexDistanceMap.keySet().parallelStream().map(indexDistanceMap::get).limit(num).collect(Collectors.toList());
    }

    @Override
    public B<Boolean> userForget(UserRegisterRequest registerRequest) {
        if (registerRequest == null) {
            return B.parameter();
        }
        String userAccount = registerRequest.getUserAccount();
        String email = registerRequest.getEmail();
        String code = registerRequest.getCode();
        String password = registerRequest.getPassword();
        String checkPassword = registerRequest.getCheckPassword();
        if (!StringUtils.hasText(userAccount)) {
            return B.parameter("账号为空");

        }
        if (!StringUtils.hasText(email)) {
            return B.parameter("邮箱为空");

        }
        if (!StringUtils.hasText(code)) {
            return B.parameter("验证码为空");

        }
        if (!StringUtils.hasText(password)) {
            return B.parameter("密码为空");

        }
        if (!StringUtils.hasText(checkPassword)) {
            return B.parameter("确认密码为空");

        }
        email = email.toLowerCase();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        User user = this.getOne(wrapper);
        if (user == null) {
            return B.parameter("该邮箱没有注册过");
        }
        String userUserAccount = user.getUserAccount();
        if (!userAccount.equals(userUserAccount)) {
            return B.parameter("请输入该邮箱绑定的账号");
        }
        String redisCode = redisCache.getCacheObject(CacheConstants.REDIS_FORGET_CODE + email);
        if (!StringUtils.hasText(redisCode)) {
            return B.parameter("验证码已过期请重试");
        }
        if (!code.equals(redisCode)) {
            return B.parameter("验证码错误");
        }
        String md5 = MD5.getMD5(password);
        user.setPassword(md5);
        boolean u = this.updateById(user);
        if (!u) {
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION, "修改错误请刷新重试");
        }
        return B.ok();
    }

    @Override
    public boolean seeUserEmail(String email) {
        if (StringUtils.hasText(email)) {
            email = email.toLowerCase();
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("email", email);
            long count = this.count(wrapper);
            return count >= 1;

        }
        return false;
    }

    // 根据邮箱查找用户
    @Override
    public User forgetUserEmail(String email) {
        if (StringUtils.hasText(email)) {
            email = email.toLowerCase();
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.select("user_account", "email");
            wrapper.eq("email", email);
            return this.getOne(wrapper);
        }
        return null;
    }

    @Override
    public List<UserAvatarVo> getUserAvatarVoByIds(List<Long> list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        return baseMapper.getUserAvatarVoByIds(list);
    }

    @Override
    public B<SafetyUserResponse> getUserVoByNameOrId(IdNameRequest idNameRequest) {
        if (idNameRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = UserUtils.getLoginUser();
        Long id = idNameRequest.getId();
        String name = idNameRequest.getName();
        if (LongUtil.isEmpty(id) && !StringUtils.hasText(name)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        if (LongUtil.isEmpty(id)) {
            if (id.equals(loginUser.getId())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "你不能查找自己");
            }
            User user = this.getById(id);
            return B.ok(UserUtils.getSafetyUserResponse(user));
        } else {
            return B.ok(baseMapper.selectByNameLike(name));

        }
    }

    @Override
    public B<String> qqLogin() {
        String requestUrl = String.format("https://uniqueker.top/connect.php?act=login&appid=%s&appkey=%s&type=qq&redirect_uri=%s",
                ConstantProperties.QQId, ConstantProperties.QQKey, "http://124.222.223.108");
        CloseableHttpClient client;
        CloseableHttpResponse response = null;
        try {
            client = HttpClients.createDefault();
            HttpGet request = new HttpGet(requestUrl);
            response = client.execute(request);

            String resp = EntityUtils.toString(response.getEntity());
            QQLogin qqLogin = GsonUtils.getGson().fromJson(resp, QQLogin.class);
            if (qqLogin.getCode() == 0) {
                return B.ok(qqLogin.getUrl());
            }
            return B.error();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error("关闭失败");
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public B<String> getQQInfo(QQLoginRequest qqLoginRequest) {
        if (qqLoginRequest == null) {
            return B.error("登录失败");
        }
        String code = qqLoginRequest.getCode();
        if (!StringUtils.hasText(code)) {
            return B.error("登录失败");
        }
        String url = "https://uniqueker.top/connect.php?act=callback&appid=1573&appkey=0473f83608b35993b558c751e77649b7&type=qq&code="+code;
        String resp = null;
        try {
            resp = httpService.get(url, String.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        QQInfo qqInfo = GsonUtils.getGson().fromJson(resp, QQInfo.class);
        if (qqInfo == null || qqInfo.getCode() != 0) {
            return B.error("登录失败");
        }
        String access_token = qqInfo.getAccess_token();
        String social_uid = qqInfo.getSocial_uid();
        String faceimg = qqInfo.getFaceimg();
        String nickname = qqInfo.getNickname();
        String location = qqInfo.getLocation();
        String gender = qqInfo.getGender();
        String ip = qqInfo.getIp();
        User userLogin = this.lambdaQuery().eq(User::getOpenId, social_uid).one();
        if (userLogin == null) {
            userLogin = new User();
            userLogin.setUsername(nickname);
            userLogin.setUserAccount(nickname);
            userLogin.setLoginType(LoginType.QQ);
            userLogin.setOpenId(social_uid);
            userLogin.setAvatarUrl(faceimg);
            userLogin.setGender(gender);
            boolean save = this.save(userLogin);
            if (!save) {
                log.error("getQQInfo 用户保存错误");
                throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
            }
        }
        return B.ok(tokenService.setToken(userLogin));

    }


}
