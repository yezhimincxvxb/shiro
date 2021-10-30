package com.yzm.shiro01.config;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class LoginFormAuthenticationFilter extends FormAuthenticationFilter {

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        try {
            WebUtils.issueRedirect(request, response, "/login?failure");
        } catch (Exception exception) {
            //
        }
        return false;
    }
}
