package com.ice.hxy.mode.constant;

/**
 * @author ice
 * @date 2022/9/10 16:59
 */

public interface CacheConstants {
    String selectFriend = "selectFriendList::";
    String TAG_REDIS_KEY = "tagNum::";

    String OSS_AVATAR_USER_REDIS_KEY = "ossAvatar:User:";
    String OSS_AVATAR_TEAM_REDIS_KEY = "ossAvatar:Team:";

    String REDIS_INDEX_KEY = "user:recommend";
    String REDIS_ADD_TEAM_LOCK = "user:addTeam:key";
    String REDIS_ADD_LOG_LOCK = "user:addLog:key";
    String REDIS_FILE_AVATAR_LOCK = "user:file:avatar:key";
    String REDIS_FILE_BY_TEAM_AVATAR_LOCK = "user:file:avatar:team:lock";
    String REDIS_FILE_BY_REGISTER_LOCK = "user:file:register:user:lock";
    String REDIS_FILE_BY_BING_DING_LOCK = "user:file:BingDing:user:lock";
    String REDIS_FILE_BY_BING_DING_KEY = "user:file:BingDing:user:key";
    String REDIS_FILE_BY_FORGET_LOCK = "user:file:forget:user:key";
    String REDIS_REGISTER_CODE = "redisRegisterCode:";
    String REDIS_FORGET_CODE = "redisRegisterCode:";

    String REDIS_POST_LIST = "redisPostList";
    String USER_TOTAL = "userTotal";

    String POST_TOTAL = "post:total:";
    String CAPTCHA_CODE_KEY = "image_code";
    String IS_LOGIN = "user:is:login:";
    /**
     * 添加定时文章(知识星球)
     */
    String ADD_POST_COOKIE_JOB_ZSXQ = "add_post_cookie_job_zsxq";
    String ADD_POST_COOKIE_JOB_XCM = "add_post_cookie_job_xcm";
    String ADD_POST_COOKIE_JOB_bcdh = "add_post_cookie_job_bcdh";


    /**
     * 令牌前缀
     */
    String LOGIN_USER_KEY = "login_user_key";
    String LOGIN_TOKEN_KEY = "user:login_tokens:";

    /**
     * 推荐文章
     */
    String MATCH_POST = "post:MATCH_POST:";
    String REGISTER = "user:register:lock";
    /**
     * 签到
     */
    String SIGN = "sign:";
    /**
     * 默认头像
     */
    String DEFAULT_AVATAR = "defaultAvatar";
    String DEFAULT_AVATAR_TEAM = "defaultTeamAvatar";
    /**
     * 是否验证码
     */
    String HAS_CODE =  "isCode";
    /**
     * 聊天未读信息
     */
    String READ_CHAT = "read_chat:";
    String DELETE_TEAM = "delete:team";
    /**
     * 公告
     */
    String NOTICE = "notice";
    /**
     * 主页文章默认推荐
     */
    String POST_INDEX_DEFAULT = "post:index:default";
}
