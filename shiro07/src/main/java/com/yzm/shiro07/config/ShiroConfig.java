package com.yzm.shiro07.config;


import com.yzm.shiro07.service.PermissionsService;
import com.yzm.shiro07.service.RoleService;
import com.yzm.shiro07.service.UserService;
import com.yzm.shiro07.utils.EncryptUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.Filter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ShiroConfig {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionsService permissionsService;

    public ShiroConfig(UserService userService, RoleService roleService, PermissionsService permissionsService) {
        this.userService = userService;
        this.roleService = roleService;
        this.permissionsService = permissionsService;
    }

    /**
     * 凭证匹配器
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new RetryLimitHashedCredentialsMatcher(redisCacheManager(), sessionManager());
        hashedCredentialsMatcher.setHashAlgorithmName(EncryptUtils.ALGORITHM_NAME);
        hashedCredentialsMatcher.setHashIterations(EncryptUtils.HASH_ITERATIONS);
        return hashedCredentialsMatcher;
    }

    /**
     * 自定义Realm
     */
    @Bean
    public MyShiroRealm simpleShiroRealm() {
        MyShiroRealm myShiroRealm = new MyShiroRealm(userService, roleService, permissionsService);
        myShiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());

        // 开启缓存
        myShiroRealm.setCachingEnabled(true);
        //启用身份验证缓存，即缓存AuthenticationInfo信息，默认false
        myShiroRealm.setAuthenticationCachingEnabled(true);
        //缓存AuthenticationInfo信息的缓存名称 在ehcache-shiro.xml中有对应缓存的配置
        myShiroRealm.setAuthenticationCacheName("authenticationCache");
        //启用授权缓存，即缓存AuthorizationInfo信息，默认false
        myShiroRealm.setAuthorizationCachingEnabled(true);
        //缓存AuthorizationInfo信息的缓存名称  在ehcache-shiro.xml中有对应缓存的配置
        myShiroRealm.setAuthorizationCacheName("authorizationCache");
        return myShiroRealm;
    }


    /**
     * redis缓存
     */
    @Bean
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost("127.0.0.1:6379");
        redisManager.setPassword("1234");
        redisManager.setDatabase(0);
        return redisManager;
    }

    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        // redis中针对不同用户缓存
        redisCacheManager.setPrincipalIdFieldName("username");
        // 用户认证权限信息缓存时间
        redisCacheManager.setExpire(200);
        return redisCacheManager;
    }

    /**
     * SessionDAO的作用是为Session提供CRUD并进行持久化的一个shiro组件
     * MemorySessionDAO 直接在内存中进行会话维护
     * EnterpriseCacheSessionDAO  提供了缓存功能的会话维护，默认情况下使用MapCache实现，内部使用ConcurrentHashMap保存缓存的会话。
     */
    @Bean
    public SessionDAO sessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        redisSessionDAO.setSessionIdGenerator(new JavaUuidSessionIdGenerator());
        //session在redis中的保存时间,最好大于session会话超时时间
        redisSessionDAO.setExpire(300);
        return redisSessionDAO;
    }

    /**
     * 配置保存sessionId的cookie
     * 注意：这里的cookie 不是上面的记住我 cookie 记住我需要一个cookie session管理 也需要自己的cookie
     */
    @Bean
    public SimpleCookie sessionIdCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("sid");
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        //maxAge=-1表示浏览器关闭时失效此Cookie
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }

    /**
     * 配置会话管理器，设定会话超时及保存
     */
    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        //监听
        sessionManager.setSessionListeners(Collections.singletonList(new MySessionListener()));

        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setSessionIdCookie(sessionIdCookie());

        sessionManager.setCacheManager(redisCacheManager());
        sessionManager.setSessionDAO(sessionDAO());

        //如果用户如果不点注销，直接关闭浏览器，不能够进行session的清空处理，所以为了防止这样的问题，还需要增加有一个会话的验证调度。
        //全局会话超时时间（单位毫秒），默认30分钟
        sessionManager.setGlobalSessionTimeout(120 * 1000L);
        //定时调度器进行检测过期session 默认为true
        sessionManager.setSessionValidationSchedulerEnabled(true);
        //设置session失效的扫描时间, 清理用户直接关闭浏览器造成的孤立会话 默认为 1个小时
        sessionManager.setSessionValidationInterval(60 * 1000L);
        //删除无效的session对象  默认为true
        sessionManager.setDeleteInvalidSessions(true);

        // 取消 JSESSIONID cookie
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        return sessionManager;
    }

    /**
     * 安全管理
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 配置单个realm
        securityManager.setRealm(simpleShiroRealm());

        // 缓存
        securityManager.setCacheManager(redisCacheManager());
        // session
        securityManager.setSessionManager(sessionManager());
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    /**
     * 开启注解
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAAP = new DefaultAdvisorAutoProxyCreator();
        defaultAAP.setProxyTargetClass(true);
        return defaultAAP;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter() {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager());

        // 自定义拦截器
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("kickOut", new KickOutSessionControlFilter());
        shiroFilterFactoryBean.setFilters(filters);

        // 拦截url
        Map<String, String> definitionMap = new LinkedHashMap<>();
        definitionMap.put("/**", "kickOut");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(definitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 问题：未登录不会自动跳转到登录页、无权访问页面不跳转
     * 原因：Shiro注解模式下，登录失败与没有权限都是通过抛出异常,并且默认并没有去处理或者捕获这些异常。
     * 解决：通过在SpringMVC下配置捕获相应异常来通知用户信息
     */
    @Bean
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
        Properties properties = new Properties();
        // 未登录访问接口跳转到/login、登录后没有权限跳转到/401
        properties.setProperty("org.apache.shiro.authz.UnauthenticatedException", "redirect:/login");
        properties.setProperty("org.apache.shiro.authz.UnauthorizedException", "redirect:/401");
        simpleMappingExceptionResolver.setExceptionMappings(properties);
        return simpleMappingExceptionResolver;
    }

}
