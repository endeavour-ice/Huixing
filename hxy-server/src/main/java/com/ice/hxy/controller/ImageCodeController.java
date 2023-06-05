package com.ice.hxy.controller;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.mode.constant.CacheConstants;
import com.ice.hxy.mode.constant.ImageConstants;
import com.ice.hxy.service.commService.RedisCache;
import com.ice.hxy.util.DateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author ice
 * @Date 2023/3/16 11:00
 * @Description: 生成验证码
 */
@RestController
public class ImageCodeController {
    @Resource
    private RedisCache redisCache;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    @CurrentLimiting
    public B<Map<String, String>> getCodeS() {
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        Map<String, String> hashMap = new HashMap<>(2);
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        CircleCaptcha captcha;
        try {
            captcha = CaptchaUtil.createCircleCaptcha(112, 38, 4, 3);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        String code = captcha.getCode();
        String imageBase64Data = captcha.getImageBase64Data();
        redisCache.setCacheObject(verifyKey, code, ImageConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        if (redisCache.hasKey("isCode")) {
            boolean isCode = redisCache.getCacheObject("isCode");
            if (!isCode) {
                hashMap.put("code", code);
            }
        }else {
            redisCache.setCacheObject("isCode", false, DateUtils.getRemainSecondsOneDay(), TimeUnit.SECONDS);
        }
        hashMap.put("uuid", uuid);
        hashMap.put("img", imageBase64Data);
        return B.ok(hashMap);
    }
}
