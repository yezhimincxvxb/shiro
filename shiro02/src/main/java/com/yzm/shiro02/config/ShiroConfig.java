package com.yzm.shiro02.config;

import com.yzm.shiro02.service.PermissionsService;
import com.yzm.shiro02.service.RoleService;
import com.yzm.shiro02.service.UserService;
import com.yzm.shiro02.utils.EncryptUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
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
        cookie.setMaxAge(120);
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
        // 配置realm
        securityManager.setRealm(shiroRealm());
        // 记住我功能
        securityManager.setRememberMeManager(rememberMeManager());
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter() {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager());
        shiroFilterFactoryBean.setLoginUrl("/login"); // 登录页url，默认会自动寻找Web工程根目录下的"/login.jsp"页面 或 "/login" 映射
        shiroFilterFactoryBean.setUnauthorizedUrl("/401"); // 访问无权限跳转url

        // 修改拦截器
        Map<String, Filter> filters = new LinkedHashMap<>();
        // 修改logout退出成功跳转url为"/login"，默认是"/"
        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setRedirectUrl("/login");
        filters.put("logout", logoutFilter);
        // 重写登录失败处理
        filters.put("authc", new LoginFormAuthenticationFilter());
        shiroFilterFactoryBean.setFilters(filters);

        Map<String, String> definitionMap = new LinkedHashMap<>();
        definitionMap.put("/", "anon");
        definitionMap.put("/home", "anon");
        definitionMap.put("/favicon.ico", "anon");
        definitionMap.put("/register", "anon");
        definitionMap.put("/401", "anon");
        definitionMap.put("/login", "authc");
        definitionMap.put("/logout", "logout");

        // 拦截器perms表示需要拥有对应的权限才可以访问
        definitionMap.put("/user/select", "perms[user:select]");
        definitionMap.put("/user/delete", "perms[user:delete]");
        // 拦截器perms[perms1,perms2]可以有多个参数，用逗号隔开，表示需要同时拥有多个权限，缺少其中一个都会被拒绝访问
        definitionMap.put("/user/createAndUpdate", "perms[user:create,user:update]");
        // 拦截器roles表示需要拥有对应的角色才可以访问，跟perms一样可以拥有多个参数
        // 由于url的定义是从上到下的，上面的定义高于下面的，比如把"/user/**"这行放到"/logout"下面，那么user角色没有对应的权限，依然可以访问上面的权限url
        definitionMap.put("/user/**", "roles[USER]");

        // 同一url可以有多个拦截器
        definitionMap.put("/admin/select", "roles[ADMIN],perms[admin:select]");
        definitionMap.put("/admin/create", "perms[admin:create]");
        definitionMap.put("/admin/update", "perms[admin:update]");
        definitionMap.put("/admin/delete", "perms[admin:delete]");
        definitionMap.put("/admin/**", "roles[ADMIN]");

        // 将 authc 改成 user
        // definitionMap.put("/**", "authc");
        definitionMap.put("/**", "user");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(definitionMap);
        return shiroFilterFactoryBean;
    }

}
