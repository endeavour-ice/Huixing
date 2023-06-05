package com.ice.hxy.config.Interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author ice
 * @Date 2022/12/8 17:55
 * @Description: web 拦截器
 */
@Slf4j
public class InterceptorConfig implements HandlerInterceptor {
    // 请求前

    @Override
    public boolean preHandle(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) throws Exception {
        return true;
    }

    // 处理后
    @Override
    public void postHandle(@Nullable HttpServletRequest request,
                           @Nullable HttpServletResponse response,
                           @Nullable Object handler, ModelAndView modelAndView) {

    }
    // 回调
    @Override
    public void afterCompletion(@Nullable HttpServletRequest request,
                                @Nullable HttpServletResponse response,
                                @Nullable Object handler, Exception ex) {

    }
}
