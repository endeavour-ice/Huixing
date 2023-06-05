package com.ice.hxy.config.filter;

import com.ice.hxy.mode.entity.User;
import com.ice.hxy.service.commService.TokenService;
import com.ice.hxy.util.UserUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Author ice
 * @Date 2023/6/4 12:36
 * @Description: TODO
 */
@Component
public class LoginFilter implements Filter {
    private final TokenService tokenService;

    public LoginFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        User user = tokenService.getTokenUser(servletRequest);
        UserUtils.setLoginUser(user);
        chain.doFilter(request,response);
        UserUtils.removeLocal();
    }
}
