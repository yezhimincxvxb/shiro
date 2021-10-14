package com.yzm.shiro06.controller;

import com.yzm.shiro06.config.MyShiroRealm;
import com.yzm.shiro06.config.RetryLimitHashedCredentialsMatcher;
import com.yzm.shiro06.entity.Role;
import com.yzm.shiro06.entity.User;
import com.yzm.shiro06.service.RoleService;
import com.yzm.shiro06.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiresRoles("ADMIN")
public class AdminController {

    private final RoleService roleService;
    private final UserService userService;

    public AdminController(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @GetMapping
    public Object admin() {
        return SecurityUtils.getSubject().getPrincipal();
    }

    @GetMapping("select")
    @RequiresPermissions("admin:select")
    public Object select() {
        return "Select";
    }

    @GetMapping("create")
    @RequiresPermissions("admin:create")
    public Object create() {
        return "Create";
    }

    @GetMapping("update")
    @RequiresPermissions("admin:update")
    public Object update() {
        return "Update";
    }

    @GetMapping("delete")
    @RequiresPermissions("admin:delete")
    public Object delete() {
        return "Delete";
    }


    /**
     * admin管理员给user用户添加 user:delete 权限(8)
     */
    @GetMapping("/addPermission")
    public String addPermission() {
        String username = "yzm";
        User yzm = userService.lambdaQuery().eq(User::getUsername, username).one();

        Role yzmR = roleService.lambdaQuery().eq(Role::getRId, yzm.getRIds()).one();
        Set<String> perms = Arrays.stream(yzmR.getPIds().split(",")).collect(Collectors.toSet());
        perms.add("8");
        yzmR.setPIds(String.join(",", perms));
        roleService.updateById(yzmR);

        // 添加成功之后 清除缓存
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        MyShiroRealm shiroRealm = (MyShiroRealm) securityManager.getRealms().iterator().next();
        // 删除指定用户的权限缓存
        SimplePrincipalCollection collection = new SimplePrincipalCollection(username, shiroRealm.getName());
        shiroRealm.getAuthorizationCache().remove(collection);

        // 删除当前登录用户的权限缓存
        //shiroRealm.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
        // 删除当前登录用户的认证和权限缓存
        //shiroRealm.clearCache(SecurityUtils.getSubject().getPrincipals());
        return "添加 user:delete 权限成功";

    }

    /**
     * admin管理员给user用户删除 user:delete 权限(8)
     */
    @GetMapping("/delPermission")
    public String delPermission() {
        String username = "yzm";
        User yzm = userService.lambdaQuery().eq(User::getUsername, username).one();

        Role yzmR = roleService.lambdaQuery().eq(Role::getRId, yzm.getRIds()).one();
        Set<String> perms = Arrays.stream(yzmR.getPIds().split(",")).collect(Collectors.toSet());
        perms.remove("8");
        yzmR.setPIds(String.join(",", perms));
        roleService.updateById(yzmR);

        //删除成功之后 清除缓存
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        MyShiroRealm shiroRealm = (MyShiroRealm) securityManager.getRealms().iterator().next();

        shiroRealm.getAuthorizationCache().remove(new SimplePrincipalCollection(username, shiroRealm.getName()));
        return "删除 user:delete 权限成功";
    }

}
