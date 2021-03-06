package com.yzm.shiro10.config;

import com.yzm.shiro10.service.PermissionsService;
import com.yzm.shiro10.service.RoleService;
import com.yzm.shiro10.service.UserService;
import com.yzm.shiro10.utils.EncryptUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.util.Arrays;
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
        return hashedCredentialsMatcher;
    }

    /**
     * UserRealm
     */
    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm(userService, roleService, permissionsService);
        userRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return userRealm;
    }

    /**
     * AdminRealm
     */
    @Bean
    public AdminRealm adminRealm() {
        AdminRealm adminRealm = new AdminRealm(userService, roleService, permissionsService);
        adminRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return adminRealm;
    }

    /**
     * 安全管理
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        // 自定义认证器、认证策略
        // FirstSuccessfulStrategy：只要有一个Realm验证成功即可，只返回第一个Realm身份验证成功的认证信息，其他的忽略；
        // AtLeastOneSuccessfulStrategy：只要有一个Realm验证成功即可，和FirstSuccessfulStrategy不同，返回所有Realm身份验证成功的认证信息；
        // AllSuccessfulStrategy：所有Realm验证成功才算成功，且返回所有Realm身份验证成功的认证信息，如果有一个失败就失败了。
        ModularRealmAuthenticator multiRealmAuthenticator = new MultiRealmAuthenticator();
        multiRealmAuthenticator.setAuthenticationStrategy(new FirstSuccessfulStrategy());
        securityManager.setAuthenticator(multiRealmAuthenticator);

        // 自定义授权器
        ModularRealmAuthorizer multiRealmAuthorizer = new MultiRealmAuthorizer();
        securityManager.setAuthorizer(multiRealmAuthorizer);

        // 多个Realm
        // 自定义realm 一定要放在securityManager.authorizer赋值之后（因为调用setRealms会将realms设置给authorizer，
        // 并给各个Realm设置permissionResolver和rolePermissionResolver）
        securityManager.setRealms(Arrays.asList(userRealm(), adminRealm()));
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
