package com.yzm.shiro09.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiresRoles("ADMIN")
@Api(value = "Admin信息", tags = {"Admin_API"})
public class AdminController {

    @ApiOperation(value = "admin", notes = "详情")
    @GetMapping
    public Object admin() {
        return SecurityUtils.getSubject().getPrincipal();
    }

    @ApiOperation(value = "select", notes = "查询")
    @GetMapping("select")
    @RequiresPermissions("admin:select")
    public Object select() {
        return "Select";
    }

    @ApiOperation(value = "create", notes = "新增")
    @GetMapping("create")
    @RequiresPermissions("admin:create")
    public Object create() {
        return "Create";
    }

    @ApiOperation(value = "update", notes = "修改")
    @GetMapping("update")
    @RequiresPermissions("admin:update")
    public Object update() {
        return "Update";
    }

    @ApiOperation(value = "delete", notes = "删除")
    @GetMapping("delete")
    @RequiresPermissions("admin:delete")
    public Object delete() {
        return "Delete";
    }

}
