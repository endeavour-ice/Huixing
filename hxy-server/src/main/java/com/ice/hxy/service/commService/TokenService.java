package com.ice.hxy.service.commService;


import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.util.SnowFlake;
import com.ice.hxy.util.SocketUtil;
import com.ice.hxy.util.Threads;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @Author ice
 * @Date 2023/4/27 10:40
 * @Description: token操作
 */
@Component
@Slf4j
public class TokenService {
    // 令牌秘钥
    private final static String secret = "ukc8!@BDbRigUDaY6pZFfWus2jZWLPHO";
    // 一分钟
    private static final long MILLISECOND = 60000;
    private static final long DAY_TIME = 24 * 60 * MILLISECOND;
    // 令牌有效期（默认5天）
    private static final long EXPIRE_TIME = 3 * DAY_TIME;
    // 四天
    private static final long MILLIS_MINUTE_TEN = 2 * DAY_TIME;
    // 刷新token时间
    private static final long REFRESH_TIME = MILLIS_MINUTE_TEN ;

    @Autowired
    private RedisCache redisCache;


    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public User getTokenUser(String token) {
        String id = parseToken(token);
        if (!StringUtils.hasText(token) || !redisCache.hasKey(getTokenKey(id))) {
            return null;
        }
        User user = redisCache.getCacheObject(getTokenKey(id));
        verifyToken(user, id, token);
        return user;
    }

    /**
     * @param request
     * @return 为空就是不存在
     */
    public User getTokenUser(HttpServletRequest request) {
        String token = getTokenByRequest(request);
        return getTokenUser(token);

    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token) {
        if (StringUtils.hasText(token)) {
            String id = parseToken(token);
            redisCache.deleteObject(getTokenKey(id));
        }
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(HttpServletRequest request) {
        String token = getTokenByRequest(request);
        if (StringUtils.hasText(token)) {
            String id = parseToken(token);
            redisCache.deleteObject(getTokenKey(id));
        }

    }

    public String getTokenByRequest(HttpServletRequest request) {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                token = cookie.getValue();
            }
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return token;
    }

    public void setTokenByUser(HttpServletRequest request, User user) {
        String token = getTokenByRequest(request);
        if (StringUtils.hasText(token)) {
            String id = parseToken(token);
            refreshToken(user, id, token, EXPIRE_TIME);
        }
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param user
     * @return 令牌
     */
    public void verifyToken(User user, String id, String token) {
        if (user != null) {
            long expireTime = user.getExpireTime();
            long currentTime = System.currentTimeMillis();
            if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
                refreshToken(user, id, token, REFRESH_TIME);
            }
        }

    }

    public void remove(HttpServletRequest request, User user) {
        String token = getTokenByRequest(request);
        String id = parseToken(token);
        if (StringUtils.hasText(id)) {
            redisCache.deleteObject(getTokenKey(id));
        }
        if (user != null) {
            String userId = CacheConstants.IS_LOGIN + user.getId();
            redisCache.deleteObject(userId);
            SocketUtil.remove(user.getId());
        }


    }

    /**
     * 将user 转换为 token
     *
     * @param user
     * @return
     */
    public String setToken(User user) {
        String userId = CacheConstants.IS_LOGIN + user.getId();
        if (redisCache.hasKey(userId)) {
            // 双删
            String t = redisCache.getCacheObject(userId);
            delLoginUser(t);
            redisCache.deleteObject(userId);
            Threads.time().execute(new TimerTask() {
                @Override
                public void run() {
                    delLoginUser(t);
                    redisCache.deleteObject(userId);
                }
            });
        }
        // 获取分布式id
        String id = SnowFlake.getSnowString();
        String token = createToken(id);
        refreshToken(user, id, token, EXPIRE_TIME);
        return token;
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(User loginUser, String id, String token, long time) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + EXPIRE_TIME);
        // 根据分布式id将loginUser缓存
        redisCache.setCacheObject(getTokenKey(id), loginUser, time, TimeUnit.MILLISECONDS);
        redisCache.setCacheObject(CacheConstants.IS_LOGIN + loginUser.getId(), token, time, TimeUnit.MILLISECONDS);

    }


    public String createToken(String id) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .claim("user", id)
                .setSubject("ice")
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }


    public String parseToken(String token, String secret) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        String id = (String) claims.get("user");
        if (!StringUtils.hasText(id)) {
            return null;
        }
        return id;
    }

    public String parseToken(String token) {
        return parseToken(token, secret);
    }

    private String getTokenKey(String id) {
        return CacheConstants.LOGIN_TOKEN_KEY + id;
    }

}
