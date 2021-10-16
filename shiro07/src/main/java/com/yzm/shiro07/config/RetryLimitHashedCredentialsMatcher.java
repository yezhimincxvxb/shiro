package com.yzm.shiro07.config;

import com.yzm.common.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.util.WebUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义登陆次数限制
 */
@Slf4j
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher {

    private final Cache<String, AtomicInteger> limitCache;
    private final Cache<String, Date> timeCache;

    // 踢出最先/最后登录的用户 默认踢出第最先登录的用户
    private boolean kickOutFirst = true;
    // 同一个帐号最大会话数 默认1
    private int maxSession = 1;

    private final SessionManager sessionManager;
    private final Cache<String, Deque<Serializable>> activeSession;

    public RetryLimitHashedCredentialsMatcher(CacheManager cacheManager, SessionManager sessionManager) {
        this.limitCache = cacheManager.getCache("limitCache");
        this.timeCache = cacheManager.getCache("timeCache");
        this.activeSession = cacheManager.getCache("activeSession");
        this.sessionManager = sessionManager;
    }

    public void setKickOutFirst(boolean kickOutFirst) {
        this.kickOutFirst = kickOutFirst;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    /**
     * 重写密码校验
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        String username = (String) token.getPrincipal();
        String usernameTime = username + "_time";
        // 获取用户登录次数
        AtomicInteger retryCount = limitCache.get(username);
        if (retryCount == null) {
            //如果用户没有登陆过,登陆次数加1 并放入缓存
            retryCount = new AtomicInteger(0);
            limitCache.put(username, retryCount);
        }

        // 如果用户登陆失败次数大于3次 锁住3分钟
        if (retryCount.incrementAndGet() > 3) {
            Date date = timeCache.get(usernameTime);
            if (date == null) {
                date = new Date();
                timeCache.put(usernameTime, date);
            }

            long time = (System.currentTimeMillis() - date.getTime()) / 1000;
            if (time <= 180) {
                log.info("锁定用户" + username);
                try {
                    WebUtils.issueRedirect(HttpUtils.getHttpServletRequest(), HttpUtils.getHttpServletResponse(), "login?time=" + (180 - time));
                } catch (Exception e) {
                    //
                }
                return false;
            } else {
                log.info("解锁用户" + username);
                limitCache.remove(username);
                timeCache.remove(usernameTime);
            }
        }

        // 判断用户账号和密码是否正确
        boolean matches = super.doCredentialsMatch(token, info);
        if (matches) {
            // 如果正确,从缓存中将用户登录计数 清除
            limitCache.remove(username);

            // 密码校验成功之后，踢出之前登录过的同一账号
            kickOut(username);
            return true;
        } else {
            try {
                log.info("密码错误：" + token.getCredentials());
                // 更新
                limitCache.put(username, retryCount);
                WebUtils.issueRedirect(HttpUtils.getHttpServletRequest(), HttpUtils.getHttpServletResponse(), "login?password");
            } catch (Exception e) {
                //
            }
            return false;
        }
    }

    private void kickOut(String username) {
        Session session = SecurityUtils.getSubject().getSession();
        Serializable sessionId = session.getId();
        Deque<Serializable> deque = activeSession.get(username);
        deque = deque != null ? deque : new LinkedList<>();
        if (!deque.contains(sessionId)) {
            deque.add(sessionId);
            // 存入数据是放在最右的
            activeSession.put(username, deque);
        }

        // 如果队列里的sessionId数超出最大会话数，开始踢人
        while (deque.size() > maxSession) {
            Serializable kickOutSessionId;
            if (kickOutFirst) {
                // 要踢出第一个登录的账号，应该拿最左边的
                kickOutSessionId = deque.removeFirst();
            } else {
                // 要踢出最后一个登录的账号，应该拿最右边的
                kickOutSessionId = deque.removeLast();
            }
            // 更新deque
            activeSession.put(username, deque);

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
    }

}
