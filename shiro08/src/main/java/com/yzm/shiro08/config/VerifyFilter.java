package com.yzm.shiro08.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 验证码过滤器
 */
@Slf4j
public class VerifyFilter extends AccessControlFilter {

    public VerifyFilter() {
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object mappedValue) throws Exception {
        HttpServletRequest request = WebUtils.toHttp(servletRequest);
        if (request.getMethod().equals("POST")) {
            //这个validateCode是在servlet中存入session的名字
            String validateCode = (String) request.getSession().getAttribute("validateCode");
            //获取用户输入的验证码
            String inputVerify = request.getParameter("verifyCode");
            log.info("用户输入：" + inputVerify);
            if (!validateCode.equalsIgnoreCase(inputVerify)) {
                WebUtils.issueRedirect(servletRequest, servletResponse, "login?verify");
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        return false;
    }

}
