package com.yzm.shiro07.config;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 并发人数控制
 */
public class KickOutSessionControlFilter extends AccessControlFilter {

    /**
     * 踢出后跳转url
     */
    private static final String kickOutUrl = "/home?kickOut";

    public KickOutSessionControlFilter() {
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated() && !subject.isRemembered()) {
            //如果没有登录，直接进行之后的流程
            return true;
        }

        // 如果被踢出了，直接退出，重定向到踢出后的地址
        Session session = subject.getSession();
        if (session.getAttribute("kickOut") != null) {
            try {
                subject.logout();
            } catch (Exception e) {
                //
            }
            WebUtils.issueRedirect(request, response, kickOutUrl);
            return false;
        }

        return true;
    }

}
