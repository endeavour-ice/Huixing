package com.ice.hxy.aop;


import com.ice.hxy.annotation.AuthSecurity;
import com.ice.hxy.common.B;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.mode.enums.UserRole;
import com.ice.hxy.mode.enums.UserStatus;
import com.ice.hxy.service.commService.TokenService;
import com.ice.hxy.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author ice
 * @Date 2023/3/18 11:44
 * @Description: TODO
 */
@Aspect
@Component
@Slf4j
public class AuthSecurityAspect {
    @Resource
    private TokenService tokenService;

    //  权限控制
    @Around(value = "@annotation(authSecurity)")// 注解标注的
    public Object doAuth(ProceedingJoinPoint pjp, AuthSecurity authSecurity) throws Throwable {
        // 获得request对象
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (sra == null) {
            return B.error("请求过快，请稍后重试");
        }
        HttpServletRequest request = sra.getRequest();
        User loginUser = tokenService.getTokenUser(request);
        if (loginUser == null) {
            return B.error(ErrorCode.NO_LOGIN);
        }
        String userRole = loginUser.getRole();
        UserRole[] noRole = authSecurity.isNoRole();
        UserRole[] role = authSecurity.isRole();
        if (role.length > 0) {
            boolean is = false;
            for (UserRole r : role) {
                String key = r.getKey();
                if (key.equals(userRole)||userRole.equals(UserRole.ROOT.getKey())) {
                    is = true;
                    break;
                }
            }
            if (!is) {
                return B.error(ErrorCode.NO_AUTH);
            }
        }
        if (noRole.length > 0) {
            boolean is = false;
            for (UserRole r : noRole) {
                if (r.getKey().equals(userRole)) {
                    is = true;
                    break;
                }
            }
            if (is) {
                return B.error(ErrorCode.NO_AUTH);
            }
        }
        // result的值就是被拦截方法的返回值
        return pjp.proceed();
    }

    /**
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.ice.hxy.controller..*.*(..))")
    public Object doUserStatusInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        try {
            User user = UserUtils.getLoginUser();
            if (user.getUserStatus().equals(UserStatus.LOCKING.getKey())) {
                return B.error("以封号，请联系管理员");
            }
        } catch (Exception ignored) {

        }
        return point.proceed();
    }
}
