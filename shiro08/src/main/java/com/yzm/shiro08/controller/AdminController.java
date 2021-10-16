package com.yzm.shiro08.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiresRoles("ADMIN")
public class AdminController {

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
}
