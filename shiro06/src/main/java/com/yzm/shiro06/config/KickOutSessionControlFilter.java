package com.yzm.shiro06.config;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 并发人数控制
 */
public class KickOutSessionControlFilter extends AccessControlFilter {

    /**
     * 踢出后跳转url
     */
    private static final String kickOutUrl = "login?kickOut";

    /**
     * 踢出最先/最后登录的用户 默认踢出第最先登录的用户
     */
    private static final boolean kickOutFirst = true;

    /**
     * 同一个帐号最大会话数 默认1
     */
    private static final int maxSession = 1;

    private final SessionManager sessionManager;
    private final Cache<String, Deque<Serializable>> cache;

    public KickOutSessionControlFilter(SessionManager sessionManager, CacheManager cacheManager) {
        this.sessionManager = sessionManager;
        this.cache = cacheManager.getCache("session");
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

        Session session = subject.getSession();
        // 如果被踢出了，直接退出，重定向到踢出后的地址
        // 由于默认是踢出最先登录的账号，退出操作可以提前判断；如果是踢出最后登录的，该判断语句放最后
        if (session.getAttribute("kickOut") != null) {
            try {
                subject.logout();
            } catch (Exception e) {
                //
            }
            WebUtils.issueRedirect(request, response, kickOutUrl);
            return false;
        }

        String username = (String) subject.getPrincipal();
        Serializable sessionId = session.getId();

        // 初始化用户的队列放到缓存里
        Deque<Serializable> deque = cache.get(username);
        if (deque == null) {
            deque = new LinkedList<>();
        }

        // 如果队列里没有此sessionId，放入队列
        if (!deque.contains(sessionId)) {
            // 最新添加的在最左边
            deque.push(sessionId);
            cache.put(username, deque);
        }

        // 如果队列里的sessionId数超出最大会话数，开始踢人
        while (deque.size() > maxSession) {
            Serializable kickOutSessionId;
            if (kickOutFirst) {
                // 要踢出第一个登录的账号，应该拿最右边的
                kickOutSessionId = deque.removeLast();
            } else {
                // 要踢出最后一个登录的账号，应该拿最左边的
                kickOutSessionId = deque.removeFirst();
            }
            // 更新deque
            cache.put(username, deque);

            try {
                Session kickOutSession = sessionManager.getSession(new DefaultSessionKey(kickOutSessionId));
                if (kickOutSession != null) {
                    //设置会话的 kickOut 属性表示踢出了
                    kickOutSession.setAttribute("kickOut", true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 退出操作
        return true;
    }

}
