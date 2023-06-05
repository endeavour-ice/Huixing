package com.ice.hxy.mode.resp;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ice
 * @date 2022/9/16 11:03
 */
@Data
public class FriendUserResponse implements Serializable {

    private static final long serialVersionUID = -6165773614351172080L;
    private String username;
    private String userAccount;
    private String avatarUrl;
    private String gender;
    private String tags;
    /**
     * 个人描述
     */
    private String profile;
    private String tel;
    private String email;
    private LocalDateTime createTime;
}
