package com.ice.hxy.extend.wx;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/3/24 20:06
 * @Description: 微信返回的数据
 */
@Data
public class MessageUserInfo {
    private int subscribe;
    private String openid;
    private String nickname;
    private int sex;
    private String language;
    private String city;
    private String province;
    private String country;
    private String headimgurl;
    private Long subscribe_time;
    private String remark;
    private int groupid;
    private String[] tagid_list;
    private String subscribe_scene;
    private int qr_scene;
    private String qr_scene_str;
}
