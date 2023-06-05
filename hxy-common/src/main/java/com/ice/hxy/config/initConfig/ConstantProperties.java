package com.ice.hxy.config.initConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author ice
 * @date 2022/9/17 21:22
 */
@Component
@Slf4j
public class ConstantProperties implements InitializingBean {


    // 读取配置文件
    @Value("${aliyun.oss.file.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.file.keyid}")
    private String keyId;

    @Value("${aliyun.oss.file.keysecret}")
    private String keySecret;

    @Value("${aliyun.oss.file.bucketname}")
    private String bucketName;

    @Value("${email.fromEmail}")
    private String email;

    @Value("${email.password}")
    private String emailPassword;
    @Value("${chatGpt.token}")
    private String token;
    @Value("${wx.appId}")
    private String appID;
    @Value("${wx.appsecret}")
    private String appsecret;
    @Value("${qq.id}")
    private String qqId;
    @Value("${qq.key}")
    private String qqKey;
    @Value("${zfb.appId}")
    private String appId;
    @Value("${zfb.merchantPrivateKey}")
    private String merchantPrivateKey;
    @Value("${zfb.alipayPublicKey}")
    private String alipayPublicKey;
    @Value("${zfb.notifyUrl}")
    private String notifyUrl;
    @Value("${zfb.returnUrl}")
    private String returnUrl;
    // 服务器地址
    public static String END_POINT;
    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String BUCKET_NAME;
    // 邮箱地址
    public static String EMAIL;
    public static String EMAILPASSWORD;
    // chatgpt
    public static String CG_TOKEN;

    // 微信配置
    public static  String APPID;
    public static  String APPSECRET;

    // QQ配置
    public static String QQId;
    public static String QQKey;
    // 支付宝配置
    public static String ZFBAPPID;
    public static String MERCHANTPRIVATEKEY;
    public static String ALIPAYPUBLICKEY;
    public static String NOTIFYURL;
    public static String RETURNURL;

    @Override
    public void afterPropertiesSet() {
        END_POINT = endpoint;
        ACCESS_KEY_ID = keyId;
        ACCESS_KEY_SECRET = keySecret;
        BUCKET_NAME = bucketName;
        EMAIL = email;
        EMAILPASSWORD = emailPassword;
        CG_TOKEN = token;
        APPID = appID;
        APPSECRET = appsecret;
        QQId = qqId;
        QQKey = qqKey;
        ZFBAPPID = appId;
        MERCHANTPRIVATEKEY = merchantPrivateKey;
        ALIPAYPUBLICKEY = alipayPublicKey;
        NOTIFYURL = notifyUrl;
        RETURNURL = returnUrl;
        log.info("ConstantProperties 读取配置文件完成");
    }
}
