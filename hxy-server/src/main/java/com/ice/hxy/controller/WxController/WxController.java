package com.ice.hxy.controller.WxController;

import com.google.gson.Gson;
import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.extend.wx.MessageEventEnum;
import com.ice.hxy.extend.wx.MessageEventInfo;
import com.ice.hxy.extend.wx.WxUtils;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.service.UserService.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author ice
 * @Date 2023/3/24 11:48
 * @Description: 微信登录公众号登录
 * TODO 未完善
 */
@RestController
@RequestMapping("wx")
@Slf4j
public class WxController {
    @Resource
    private IUserService userService;
    // 获取二维码
    @GetMapping("url")
    @CurrentLimiting
    @AuthSecurity(isRole = {UserRole.ADMIN})// 权限控制
    public B<String> getUrl() {
        String url = WxClintApi.getQRCodeUrl();
        return B.ok(url);
    }
    // 微信确认回调
    @GetMapping("/resp")
    @AuthSecurity(isRole = {UserRole.ADMIN})
    public String getUrl(@RequestParam(name = "signature", required = false) String signature,
                       @RequestParam(name = "timestamp", required = false) String timestamp,
                       @RequestParam(name = "nonce", required = false) String nonce,
                       @RequestParam(name = "echostr", required = false) String echostr,HttpServletRequest request) {
        return WxUtils.wxOfficialTokenCheck(signature, timestamp, nonce,echostr);
    }
    // 微信回调
    @PostMapping("/resp")
    @AuthSecurity(isRole = {UserRole.ADMIN})
    public String getWxUrlCallback(HttpServletRequest request) {
        try {
            Map<String, String> map = WxUtils.xmlToMap(request.getInputStream());
            Gson gson = GsonUtils.getGson();
            String json = gson.toJson(map);
            MessageEventInfo messageEventInfo = gson.fromJson(json, MessageEventInfo.class);
            String openId = messageEventInfo.getFromUserName();
            String event = messageEventInfo.getEvent();
            String sceneId;
            if (MessageEventEnum.SUBSCRIBE.getName().equals(event)) {
                //关注
                log.info("用户{}关注公众号", openId);
                sceneId = messageEventInfo.getEventKey().replace("qrscene_", "");
                messageEventInfo.setEventKey(sceneId);
                //将场景值或者ticket保存到redis

            } else if (MessageEventEnum.SCAN.getName().equals(event)) {
                //扫码
                log.info("用户{}扫描公众号二维码", openId);

                //将场景值或者Ticket保存到redis

            } else if (MessageEventEnum.UNSUBSCRIBE.getName().equals(event)) {
                //取消关注
                log.info("用户{}取消关注公众号", openId);
            }
        } catch (Exception e) {
            log.error("微信公众号事件回调接口异常：{}", e.getMessage());
            return "ERROR";
        }
        return "SUCCESS";

    }
}
