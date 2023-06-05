package com.ice.hxy.config.filter;

import com.ice.hxy.service.commService.TokenService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.Filter;

/**
 * @Author ice
 * @Date 2023/4/25 10:24
 * @Description: 过滤器配置
 */
@Configuration
public class FilterConfig {
    @Resource
    private TokenService tokenService;
    @Bean
    public FilterRegistrationBean<Filter> LoginFilterConfig(){
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoginFilter(tokenService));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("loginFilter");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
