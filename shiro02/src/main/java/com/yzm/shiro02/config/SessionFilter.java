package com.yzm.shiro02.config;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义拦截器
 * 解决使用记住我功能之后，会话无效问题
 */
public class SessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Subject subject = SecurityUtils.getSubject();
        //判断用户是通过记住我功能自动登录,此时session失效
        //记住我的功能isAuthenticated肯定为false 而isRemembered肯定为true (因为短时间不用认证)
        if (!subject.isAuthenticated() && subject.isRemembered()) {
            try {
                Session session = subject.getSession();
                if (session.getAttribute("username") == null) {
                    String username = (String) subject.getPrincipal();
                    session.setAttribute("username", username);
                    //设置会话的过期时间--ms,默认是30分钟，设置负数表示永不过期
                    session.setTimeout(-1L);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        filterChain.doFilter(request, response);
    }
}
