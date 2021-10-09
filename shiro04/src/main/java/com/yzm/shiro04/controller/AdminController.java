package com.yzm.shiro04.controller;

import com.yzm.shiro04.config.SimpleShiroRealm;
import com.yzm.shiro04.entity.Role;
import com.yzm.shiro04.service.RoleService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
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

    public AdminController(RoleService roleService) {
        this.roleService = roleService;
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
     * 给admin用户添加 admin:delete 权限
     */
    @GetMapping("/addPermission")
    public String addPermission() {

        Role admin = roleService.lambdaQuery().eq(Role::getRName, "ADMIN").one();
        Set<String> perms = Arrays.stream(admin.getPIds().split(",")).collect(Collectors.toSet());
        perms.add("4");
        admin.setPIds(String.join(",", perms));
        roleService.updateById(admin);

        //添加成功之后 清除缓存
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        SimpleShiroRealm shiroRealm = (SimpleShiroRealm) securityManager.getRealms().iterator().next();
        // 删除所有人的缓存
        shiroRealm.clearAllCache();
        return "给admin用户添加 admin:delete 权限成功";

    }

    /**
     * 删除admin用户 admin:delete 权限
     */
    @GetMapping("/delPermission")
    public String delPermission() {

        Role admin = roleService.lambdaQuery().eq(Role::getRName, "ADMIN").one();
        Set<String> perms = Arrays.stream(admin.getPIds().split(",")).collect(Collectors.toSet());
        perms.remove("4");
        admin.setPIds(String.join(",", perms));
        roleService.updateById(admin);

        //删除成功之后 清除缓存
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        SimpleShiroRealm shiroRealm = (SimpleShiroRealm) securityManager.getRealms().iterator().next();
        //shiroRealm.clearAllCache();
        //删除当前登录用户的授权缓存
        //shiroRealm.getAuthorizationCache().remove(SecurityUtils.getSubject().getPrincipals());
        shiroRealm.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
        return "删除admin用户 admin:delete 权限成功";
    }

}
