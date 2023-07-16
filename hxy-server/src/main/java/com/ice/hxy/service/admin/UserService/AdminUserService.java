package com.ice.hxy.service.admin.UserService;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.request.UserSearchPage;
import com.ice.hxy.mode.request.admin.UserAuthReq;
import com.ice.hxy.mode.request.admin.UserStatusReq;

public interface AdminUserService {
    B<Page<UserVo>> list(UserSearchPage searchPage);

    B<Boolean> upRole(UserAuthReq userAuthReq);

    B<Boolean> upStatus(UserStatusReq userStatusReq);
}
