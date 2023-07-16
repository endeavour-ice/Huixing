package com.ice.hxy.controller.UserController.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.entity.vo.UserVo;
import com.ice.hxy.mode.request.UserSearchPage;
import com.ice.hxy.mode.request.admin.UserAuthReq;
import com.ice.hxy.mode.request.admin.UserStatusReq;
import com.ice.hxy.service.admin.UserService.AdminUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    public B<Page<UserVo>> list(@RequestBody UserSearchPage searchPage) {
        return adminUserService.list(searchPage);
    }

    @PostMapping("/up/role")
    public B<Boolean> upRole(@RequestBody UserAuthReq userAuthReq) {
        return adminUserService.upRole(userAuthReq);
    }

    @PostMapping("/up/status")
    public B<Boolean> upStatus(@RequestBody UserStatusReq userStatusReq) {
        return adminUserService.upStatus(userStatusReq);
    }
}
