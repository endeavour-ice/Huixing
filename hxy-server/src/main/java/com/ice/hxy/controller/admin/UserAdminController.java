package com.ice.hxy.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.admin.AdminSafetyUserResp;
import com.ice.hxy.mode.entity.admin.AdminUserResp;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.request.UserLoginRequest;
import com.ice.hxy.mode.request.UserSearchPage;
import com.ice.hxy.mode.request.admin.UserAuthReq;
import com.ice.hxy.mode.request.admin.UserStatusReq;
import com.ice.hxy.service.admin.AdminUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author ice
 * @Date 2023/7/9 17:56
 * @Description: TODO
 */
@RestController
@RequestMapping("/admin/user")
public class UserAdminController {
    @Resource
    private AdminUserService adminUserService;

    @PostMapping("/get")
    @AuthSecurity(isRole = UserRole.ADMIN)
    public B<Page<AdminUserResp>> list(@RequestBody UserSearchPage searchPage) {
        return adminUserService.list(searchPage);
    }

    @GetMapping("/current")
    @CurrentLimiting
    @AuthSecurity(isRole = UserRole.ADMIN)
    public B<AdminSafetyUserResp> getCurrent(HttpServletRequest request) {
        return adminUserService.getCurrent(request);
    }

    // 用户登录
    @PostMapping("/Login")
    @CurrentLimiting
    public B<String> userAdminLogin(@RequestBody UserLoginRequest userLogin) {
        return adminUserService.userAdminLogin(userLogin);
    }

    @PostMapping("/up/role")
    @AuthSecurity(isRole = UserRole.ADMIN)
    public B<Boolean> upRole(@RequestBody UserAuthReq userAuthReq) {
        return adminUserService.upRole(userAuthReq);
    }

    @PostMapping("/up/status")
    @AuthSecurity(isRole = UserRole.ADMIN)
    public B<Boolean> upStatus(@RequestBody UserStatusReq userStatusReq) {
        return adminUserService.upStatus(userStatusReq);
    }
}
