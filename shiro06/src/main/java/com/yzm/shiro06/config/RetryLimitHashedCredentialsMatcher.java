package com.yzm.shiro06.config;

import com.yzm.common.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.web.util.WebUtils;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义登陆次数限制
 */
@Slf4j
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher {

    private final Cache<String, AtomicInteger> limitCache;
    private final Cache<String, Date> timeCache;

    public RetryLimitHashedCredentialsMatcher(CacheManager cacheManager) {
        this.limitCache = cacheManager.getCache("limitCache");
        this.timeCache = cacheManager.getCache("timeCache");
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        String username = (String) token.getPrincipal();
        String usernameTime = username + "_time";
        // 获取用户登录次数、初次登录时间
        AtomicInteger retryCount = limitCache.get(username);
        if (retryCount == null) {
            //如果用户没有登陆过,登陆次数加1 并放入缓存
            retryCount = new AtomicInteger(0);
            limitCache.put(username, retryCount);
        }

        // 如果用户登陆失败次数大于5次 锁住3分钟
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
            return true;
        } else {
            try {
                // 更新
                limitCache.put(username, retryCount);
                WebUtils.issueRedirect(HttpUtils.getHttpServletRequest(), HttpUtils.getHttpServletResponse(), "login?password");
            } catch (Exception e) {
                //
            }
            return false;
        }
    }

}
