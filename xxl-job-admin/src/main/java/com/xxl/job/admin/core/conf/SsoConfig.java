package com.xxl.job.admin.core.conf;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.sekorm.sso.common.Conf;
import com.sekorm.sso.filter.WebFilter;
import com.sekorm.sso.util.JedisUtils;
import com.xxl.job.admin.controller.filter.UserSessionFilter;

@Order(1)
@Configuration
public class SsoConfig implements DisposableBean {

    @Bean
    public FilterRegistrationBean ssoFilterRegistration() {
        JedisUtils.init("redis://172.16.1.94:6379");
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setName("SsoWebFilter");
        registration.setOrder(0);
        registration.addUrlPatterns("/*");
        registration.setFilter(new WebFilter());
        registration.addInitParameter(Conf.SSO_SERVER, "http://mm.sekorm.com:8999/sekorm-sso-server");
        registration.addInitParameter(Conf.SSO_EXCLUDED_PATHS,
            "/**/css/**,/**/fonts/**,/**/images/**,/**/img/**,/**/js/**,/**/plugins/**,/**/download**");
        registration.addInitParameter(Conf.SSO_LOGOUT_PATH, "/login/out");
        registration.addInitParameter(Conf.SSO_INDEX, "/login/out/index");
        return registration;
    }

    @Bean
    public FilterRegistrationBean UserSessionFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setName("UserSessionFilter");
        registration.setOrder(1);
        registration.addUrlPatterns("/*");
        registration.setFilter(new UserSessionFilter());
        return registration;
    }

    @Override
    public void destroy() throws Exception {
        JedisUtils.close();
    }

}
