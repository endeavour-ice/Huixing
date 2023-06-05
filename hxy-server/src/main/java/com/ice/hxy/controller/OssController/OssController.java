package com.ice.hxy.controller.OssController;

import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.service.OssService.OssService;
import com.ice.hxy.util.ResponseEmail;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * @author ice
 * @date 2022/9/17 12:46
 */
@RestController
@RequestMapping("/oss")
public class OssController {

    @Resource
    private OssService ossService;



    /**
     * 用户头像上传
     * @param file
     * @param request
     * @return
     */
    @PostMapping("/file/upload")
    @CurrentLimiting
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<String> upFile(MultipartFile file, HttpServletRequest request) {
        return ossService.upload(file, request);
    }

    /**
     * 队伍头像上传
     * @param file
     * @param teamID
     * @return
     */
    @PostMapping("/file/upload/team/{teamID}")
    @CurrentLimiting
    @AuthSecurity(isNoRole = {UserRole.TEST})
    public B<String> upFileByTeam(MultipartFile file, @PathVariable Long teamID) {

        return ossService.upFileByTeam(file, teamID);

    }

    /**
     * 注册邮箱验证
     *
     * @param email
     * @return
     */
    @PostMapping("/send")
    @CurrentLimiting
    public B<Boolean> sendEMail(@RequestBody ResponseEmail email, HttpServletRequest request) {
        return ossService.sendRegisterEMail(email, request);
    }

    /**
     * 忘记密码邮箱验证
     *
     * @param email
     * @return
     */
    @PostMapping("/sendForget")
    @CurrentLimiting
    public B<Boolean> sendForgetEMail(@RequestBody ResponseEmail email, HttpServletRequest request) {
        return ossService.sendForgetEMail(email, request);
    }

    /**
     * 发送绑定邮件的验证码
     *
     * @param email   邮件
     * @param request
     * @return
     */
    @PostMapping("/sendBinDing")
    @CurrentLimiting
    public B<Boolean> sendBinDingEMail(@RequestBody ResponseEmail email, HttpServletRequest request) {
        return ossService.sendBinDingEMail(email, request);

    }
}
