package com.yzm.shiro10.controller;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiresRoles(value = {"USER", "ADMIN"}, logical = Logical.OR)
public class UserController {

    @GetMapping
    public Object user() {
        return SecurityUtils.getSubject().getPrincipal();
    }

    @GetMapping("select")
    @RequiresPermissions("user:select")
    public Object select() {
        return "Select";
    }

    @GetMapping("create")
    @RequiresPermissions("user:create")
    public Object create() {
        return "Create";
    }

    @GetMapping("update")
    @RequiresPermissions("user:update")
    public Object update() {
        return "Update";
    }

    @GetMapping("delete")
    @RequiresPermissions("user:delete")
    public Object delete() {
        return "Delete";
    }

}
