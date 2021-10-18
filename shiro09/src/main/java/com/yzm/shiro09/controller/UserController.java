package com.yzm.shiro09.controller;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Yzm
 * @since 2021-10-08
 */
@RestController
@RequestMapping("//user")
@RequiresRoles(value = {"USER", "ADMIN"}, logical = Logical.OR)
public class UserController {

    @GetMapping
    public Object user() {
        // 当使用noSessionCreation时，这里就会报错
        System.out.println("会话ID：" + SecurityUtils.getSubject().getSession().getId());
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

    @GetMapping("createAndUpdate")
    //需要同时拥有
    @RequiresPermissions(value = {"user:create", "user:update"}, logical = Logical.AND)
    public Object createAndUpdate() {
        return "Create And Update";
    }

    @GetMapping("createOrUpdate")
    //拥有其中任意一个即可
    @RequiresPermissions(value = {"user:create", "user:update"}, logical = Logical.OR)
    public Object createOrUpdate() {
        return "Create Or Update";
    }

}
