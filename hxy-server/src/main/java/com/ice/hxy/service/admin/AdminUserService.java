package com.ice.hxy.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.admin.AdminSafetyUserResp;
import com.ice.hxy.mode.entity.admin.AdminUserResp;
import com.ice.hxy.mode.request.UserLoginRequest;
import com.ice.hxy.mode.request.UserSearchPage;
import com.ice.hxy.mode.request.admin.UserAuthReq;
import com.ice.hxy.mode.request.admin.UserStatusReq;

import javax.servlet.http.HttpServletRequest;

public interface AdminUserService {
    B<Page<AdminUserResp>> list(UserSearchPage searchPage);

    B<Boolean> upRole(UserAuthReq userAuthReq);

    B<Boolean> upStatus(UserStatusReq userStatusReq);

    B<AdminSafetyUserResp> getCurrent(HttpServletRequest request);

    B<String> userAdminLogin(UserLoginRequest userLogin);
}
