package com.ice.hxy.controller.WxController;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.ice.hxy.config.initConfig.ConstantProperties;
import com.ice.hxy.extend.wx.MessageUserInfo;
import com.ice.hxy.extend.wx.WxOfficalTicket;
import com.ice.hxy.extend.wx.WxToken;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.GsonUtils;

import com.ice.hxy.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author ice
 * @Date 2023/3/24 14:20
 * @Description: TODO
 */
@Slf4j
public class WxClintApi {
    private static final RedisCache redisCache = SpringUtil.getBean(RedisCache.class);
    private static final String tokenKey = "tokenKey";
    private static final String ticketKey = "ticketKey";
    /**
     * 获取access_token
     */
    private static final String GET_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

    /**
     * 获取创建二维码ticket
     */
    private static final String GET_TICKET = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";

    /**
     * 创建二维码
     */
    private static final String GET_QR_CODE = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s";

    /**
     * 获取用户信息
     */
    private static final String GET_USER_INFO = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s";

    private static final Gson gson = GsonUtils.getGson();

    private static String getAccessTokenUrl(String appId, String appSecret) {
        return String.format(GET_ACCESS_TOKEN, appId, appSecret);
    }

    private static String getTicketUrl(String accessToken) {
        return String.format(GET_TICKET, accessToken);
    }

    public static String getQRCodeUrl(String ticket) {
        return String.format(GET_QR_CODE, ticket);
    }
    public static String getQRCodeUrl() {
        return String.format(GET_QR_CODE, getTicket(getAccessToken()));
    }
    private static String getUserInfoUrl(String accessToken, String openId) {
        return String.format(GET_USER_INFO, accessToken, openId);
    }

    public static MessageUserInfo getUserInfo(String accessToken, String openId) {
        return gson.fromJson(getUserInfoUrl(accessToken, openId), MessageUserInfo.class);
    }
    //
    ///**
    // * 获取access_token
    // */
    public static String getAccessToken() {

        if (redisCache.hasKey(tokenKey)) {
            return redisCache.getCacheObject(tokenKey);
        }else {
            WxToken wxToken = gson.fromJson(HttpUtil.get(getAccessTokenUrl(ConstantProperties.APPID, ConstantProperties.APPSECRET)), WxToken.class);
            String access_token = wxToken.getAccess_token();
            Long expires_in = wxToken.getExpires_in();
            redisCache.setCacheObject(tokenKey, access_token, expires_in.intValue(), TimeUnit.SECONDS);
            return access_token;
        }
    }

    /**
     * 根据access_token获取Ticket
     */
    public static String getTicket(String accessToken) {
        if (redisCache.hasKey(ticketKey)) {
            return redisCache.getCacheObject(ticketKey);
        }else {
            HttpResponse httpResponse = HttpRequest.post(getTicketUrl(accessToken))
                    .body(String.format("{\"expire_seconds\": 604800, \"action_name\": \"QR_STR_SCENE\"," +
                            " \"action_info\": {\"scene\": {\"scene_str\": \"%s\"}}}", "登录")).execute();
            String body = httpResponse.body();
            WxOfficalTicket ticket = gson.fromJson(body, WxOfficalTicket.class);
            String ticketTicket = ticket.getTicket();
            Long expire_seconds = ticket.getExpire_seconds();
            redisCache.setCacheObject(ticketTicket, ticketTicket, expire_seconds.intValue(), TimeUnit.SECONDS);
            return ticketTicket;
        }


    }



}
