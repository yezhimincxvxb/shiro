package com.yzm.shiro01.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@Slf4j
public class LoginFormAuthenticationFilter extends FormAuthenticationFilter {

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        try {
            log.info("错误信息：" + e.getMessage());
            WebUtils.issueRedirect(request, response, "/login?failure");
        } catch (Exception exception) {
            //
        }
        return false;
    }
}
