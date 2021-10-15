package com.yzm.shiro04.config;


import com.yzm.shiro04.service.PermissionsService;
import com.yzm.shiro04.service.RoleService;
import com.yzm.shiro04.service.UserService;
import com.yzm.shiro04.utils.EncryptUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

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
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName(EncryptUtils.ALGORITHM_NAME);
        hashedCredentialsMatcher.setHashIterations(EncryptUtils.HASH_ITERATIONS);
        //true加密用的hex编码，false用的base64编码;默认true，本实例是toHex，可以查看EncryptUtils
        //hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
        return hashedCredentialsMatcher;
    }

    /**
     * 用户realm
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
     * 记住我功能
     */
    @Bean
    public Cookie simpleCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        //设为true后，只能通过http访问，javascript无法访问
        //防止xss读取cookie
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        //存活时间，单位秒；-1表示关闭浏览器该cookie失效
        cookie.setMaxAge(-1);
        return cookie;
    }

    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
        rememberMeManager.setCookie(simpleCookie());
        //cookie加密的密钥
        //rememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return rememberMeManager;
    }

    /**
     * ehcache缓存
     */
    @Bean
    public EhCacheManager ehCacheManager() {
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManagerConfigFile("classpath:config/ehcache-shiro.xml");
        return cacheManager;
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
        // 用户权限信息缓存时间
        redisCacheManager.setExpire(300);
        return redisCacheManager;
    }

    /**
     * 安全管理
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 配置单个realm
        securityManager.setRealm(simpleShiroRealm());
        // 记住我
        securityManager.setRememberMeManager(rememberMeManager());
        // 缓存
        securityManager.setCacheManager(redisCacheManager());
        return securityManager;
    }

    /**
     * 开启注解方式控制访问url
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
        // setLoginUrl 如果不设置值，默认会自动寻找Web工程根目录下的"/login.jsp"页面 或 "/login" 映射
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 设置无权限时跳转的 url
        shiroFilterFactoryBean.setUnauthorizedUrl("/401");

//        Map<String, String> definitionMap = new LinkedHashMap<>();
//        definitionMap.put("/home", "anon");
//        shiroFilterFactoryBean.setFilterChainDefinitionMap(definitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 解决： 无权限页面不跳转 shiroFilterFactoryBean.setUnauthorizedUrl("/401") 无效
     * shiro的源代码ShiroFilterFactoryBean.Java定义的filter必须满足filter instanceof AuthorizationFilter，
     * 只有perms，roles，ssl，rest，port才是属于AuthorizationFilter，而anon，authcBasic，auchc，user是AuthenticationFilter，
     * 所以unauthorizedUrl设置后页面不跳转 Shiro注解模式下，登录失败与没有权限都是通过抛出异常。
     * 并且默认并没有去处理或者捕获这些异常。在SpringMVC下需要配置捕获相应异常来通知用户信息
     */
    @Bean
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
        Properties properties = new Properties();
        properties.setProperty("org.apache.shiro.authz.UnauthorizedException", "/401");
        properties.setProperty("org.apache.shiro.authz.UnauthenticatedException", "/login");
        simpleMappingExceptionResolver.setExceptionMappings(properties);
        return simpleMappingExceptionResolver;
    }

    /**
     * 让某个实例的某个方法的返回值注入为Bean的实例
     */
    @Bean
    public MethodInvokingFactoryBean getMethodInvokingFactoryBean() {
        MethodInvokingFactoryBean factoryBean = new MethodInvokingFactoryBean();
        factoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        //factoryBean.setArguments(new Object[]{securityManager()});
        factoryBean.setArguments(securityManager());
        return factoryBean;
    }

}
