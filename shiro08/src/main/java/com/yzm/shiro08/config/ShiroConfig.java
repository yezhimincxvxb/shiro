package com.yzm.shiro08.config;


import com.yzm.shiro08.service.PermissionsService;
import com.yzm.shiro08.service.RoleService;
import com.yzm.shiro08.service.UserService;
import com.yzm.shiro08.utils.EncryptUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.Filter;
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
     * 注入验证码servlet
     * servlet可拦截指定url路径，添加自定义操作
     */
    @Bean
    public ServletRegistrationBean<VerifyServlet> initServletRegistrationBean() {
        return new ServletRegistrationBean<>(new VerifyServlet(), "/getVerifyCode");
    }

    /**
     * 凭证匹配器
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
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
        rememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return rememberMeManager;
    }

    /**
     * 安全管理SecurityManager
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 配置单个realm
        securityManager.setRealm(simpleShiroRealm());
        securityManager.setRememberMeManager(rememberMeManager());
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
        filters.put("verify", new VerifyFilter());
        shiroFilterFactoryBean.setFilters(filters);

        // 拦截url
        Map<String, String> definitionMap = new LinkedHashMap<>();
        definitionMap.put("/getVerifyCode", "anon");
        definitionMap.put("/login", "verify");
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
