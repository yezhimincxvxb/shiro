package com.yzm.shiro01.config;

import com.yzm.shiro01.service.PermissionsService;
import com.yzm.shiro01.service.RoleService;
import com.yzm.shiro01.service.UserService;
import com.yzm.shiro01.utils.EncryptUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
     * 用户realm
     */
    @Bean(name = "simpleRealm")
    public SimpleShiroRealm simpleShiroRealm() {
        SimpleShiroRealm simpleShiroRealm = new SimpleShiroRealm(userService, roleService, permissionsService);
        // 凭证匹配器 密码加密解密
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName(EncryptUtils.ALGORITHM_NAME);
        hashedCredentialsMatcher.setHashIterations(EncryptUtils.HASH_ITERATIONS);
        //true加密用的hex编码，false用的base64编码;默认true
        //hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
        simpleShiroRealm.setCredentialsMatcher(hashedCredentialsMatcher);
        return simpleShiroRealm;
    }

    /**
     * 安全管理
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 配置单个realm
        securityManager.setRealm(simpleShiroRealm());
        return securityManager;
    }

    /**
     * 通过配置方式控制访问url
     */
    @Bean
    public DefaultShiroFilterChainDefinition definition() {
        DefaultShiroFilterChainDefinition definition = new DefaultShiroFilterChainDefinition();
        //拦截器anon，无参数，表示匿名访问
        definition.addPathDefinition("/home", "anon");
        definition.addPathDefinition("/login", "anon");
        definition.addPathDefinition("/401", "anon");
        definition.addPathDefinition("/register", "anon");
        definition.addPathDefinition("/doLogin", "anon");
        definition.addPathDefinition("/logout", "logout");
        //definition.addPathDefinition("/user/**", "roles[USER]");
        //拦截器perms表示需要拥有对应的权限才可以访问
        definition.addPathDefinition("/user/select", "perms[user:select]");
        definition.addPathDefinition("/user/create", "perms[user:create]");
        definition.addPathDefinition("/user/update", "perms[user:update]");
        definition.addPathDefinition("/user/delete", "perms[user:delete]");
        //拦截器perms[perms1,perms2]可以有多个参数，用逗号隔开，表示需要同时拥有多个权限，缺少其中一个都会被拒绝访问
        definition.addPathDefinition("/user/createAndUpdate", "perms[user:create,user:update]");
        //拦截器roles表示需要拥有对应的角色才可以访问，跟perms一样可以拥有多个参数
        //由于url的定义是从上到下的，上面的定义高于下面的，比如把"/user/**"这行放到"/logout"下面，那么user角色没有对应的权限，依然可以访问上面的权限url
        definition.addPathDefinition("/user/**", "roles[USER]");
        //同一url可以有多个拦截器
        definition.addPathDefinition("/admin/select", "roles[ADMIN],perms[admin:select]");
        definition.addPathDefinition("/admin/create", "perms[admin:create]");
        definition.addPathDefinition("/admin/update", "perms[admin:update]");
        definition.addPathDefinition("/admin/delete", "perms[admin:delete]");
        definition.addPathDefinition("/admin/**", "roles[ADMIN]");
        definition.addPathDefinition("/**", "authc");
        return definition;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter() {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager());
        // setLoginUrl 如果不设置值，默认会自动寻找Web工程根目录下的"/login.jsp"页面 或 "/login" 映射
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 设置无权限时跳转的 url
        shiroFilterFactoryBean.setUnauthorizedUrl("/401");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(definition().getFilterChainMap());
        return shiroFilterFactoryBean;
    }

}
