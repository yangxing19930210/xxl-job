package com.xxl.job.admin.controller.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.AntPathMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sekorm.sso.common.Conf;
import com.sekorm.sso.entity.UserInfoEntity;

/**
 * session过滤器
 *
 * @author noah_yang
 * @version 1.0
 * @create 2018-09-25 11:49
 */

public class UserSessionFilter implements Filter {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final String excludedPaths =
        "/**/css/**,/**/fonts/**,/**/images/**,/**/img/**,/**/js/**,/**/plugins/**,/**/download**";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
        String url = httpServletRequest.getServletPath();
        for (String excludedPath : excludedPaths.split(",")) {
            String uriPattern = excludedPath.trim();
            // 支持ANT表达式
            if (antPathMatcher.match(uriPattern, url)) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }
        }
        UserInfoEntity userInfoEntity = (UserInfoEntity)httpServletRequest.getAttribute(Conf.SSO_USER);
        if (userInfoEntity != null) {
            httpServletRequest.setAttribute("name", userInfoEntity.getUserName());
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
    }

    @Override
    public void destroy() {}
}
