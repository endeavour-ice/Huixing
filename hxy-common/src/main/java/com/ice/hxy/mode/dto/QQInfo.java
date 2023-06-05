package com.ice.hxy.mode.dto;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/4/26 17:38
 * @Description: 返回的qq用户信息
 */
@Data
public class QQInfo {
    /*
    {
        “code”: 0,
        “msg”: “succ”,
        “type”: “qq”,
        “access_token”: “89DC9691E274D6B596FFCB8D43368234”,
        “social_uid”: “AD3F5033279C8187CBCBB29235D5F827”,
        “faceimg”: “https:// thirdqq.qlogo.cn/g?b=oidb&k=3WrWp3peBxlW4MFxDgDJEQ&s=100&t=1596856919”,
        “nickname”: “大白”,
        “location”: “XXXXX市”,
        “gender”: “男”,
        “ip”: “1.12.3.40”
}
     */
    private Integer code;
    private String msg;
    private String type;
    private String access_token;
    private String social_uid;
    private String faceimg;
    private String nickname;
    private String location;
    private String gender;
    private String ip;
}
