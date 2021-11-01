package com.yzm.shiro09.config;

import com.yzm.shiro09.service.PermissionsService;
import com.yzm.shiro09.service.RoleService;
import com.yzm.shiro09.service.UserService;
import com.yzm.shiro09.utils.EncryptUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

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
     * 自定义Realm
     */
    @Bean
    public MyShiroRealm shiroRealm() {
        MyShiroRealm shiroRealm = new MyShiroRealm(userService, roleService, permissionsService);
        shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return shiroRealm;
    }

    /**
     * jwtRealm
     */
    @Bean
    public JwtRealm jwtRealm() {
        JwtRealm jwtRealm = new JwtRealm(userService);
        jwtRealm.setCredentialsMatcher(new JwtCredentialsMatcher());
        return jwtRealm;
    }

    /**
     * 禁用session, 不保存用户登录状态。保证每次请求都重新认证
     */
    @Bean
    protected SessionStorageEvaluator sessionStorageEvaluator() {
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        return sessionStorageEvaluator;
    }

    /**
     * 安全管理
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 配置单个realm
        //securityManager.setRealm(shiroRealm());

        // 验证器
        ModularRealmAuthenticator authenticator = new MultiRealmAuthenticator();
        authenticator.setAuthenticationStrategy(new FirstSuccessfulStrategy());
        securityManager.setAuthenticator(authenticator);

        // 自定义realm 一定要放在securityManager.authorizer赋值之后（因为调用setRealms会将realms设置给authorizer
        securityManager.setRealms(Arrays.asList(shiroRealm(), jwtRealm()));

        // 关闭shiro自带的session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator());
        securityManager.setSubjectDAO(subjectDAO);
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

        // 注入自定义拦截器
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("autht", new JwtFilter());
        shiroFilterFactoryBean.setFilters(filters);

        Map<String, String> definitionMap = new LinkedHashMap<>();
        definitionMap.put("/swagger**", "anon");
        definitionMap.put("/webjars/**", "anon");
        definitionMap.put("/swagger-resources/**", "anon");
        definitionMap.put("/v2/**", "anon");

        definitionMap.put("/login", "anon");

        // 禁用session创建，还需要noSessionCreation
        definitionMap.put("/**", "noSessionCreation,autht");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(definitionMap);
        return shiroFilterFactoryBean;
    }

}
