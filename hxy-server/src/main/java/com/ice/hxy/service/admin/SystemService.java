package com.ice.hxy.service.admin;

import com.ice.hxy.common.B;
import com.ice.hxy.mode.request.admin.NoticeReq;
import com.ice.hxy.mode.request.admin.PostCookie;
import com.ice.hxy.mode.request.admin.PostZSReq;
import com.ice.hxy.mode.resp.admin.PostSortedResp;
import com.ice.hxy.mode.resp.tag.TagResp;

import java.util.List;

public interface SystemService {
    /**
     * 更新用户的头像
     * @param cookie 可以为空
     * @return
     */
    B<Boolean> upUserAvUrl(String cookie);

    B<Boolean> postCookie(PostCookie cookie);

    /**
     * 设置默认用户注册时的头像
     * @param av 使用json数据
     * @return
     */
    B<Boolean> defaultUserUrl(String av);

    B<Boolean> defaultTeamUrl(String tav);

    B<List<PostCookie>> getCookie();

    B<Boolean> notice(NoticeReq notice);

    B<String> getNotice();

    B<TagResp> getTag();

    B<Boolean> defaultPostIndex(String value);

    B<List<PostSortedResp>> getDefaultPostIndex();

    B<Void> upStr();

    B<Void> pqZS(PostZSReq postZSReq);
}
