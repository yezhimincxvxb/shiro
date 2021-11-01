package com.yzm.shiro09.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "User信息", tags = {"User_API"})
public class UserController {

    @ApiOperation(value = "user", notes = "详情")
    @GetMapping
    public Object user() {
        return SecurityUtils.getSubject().getPrincipal();
    }

    @ApiOperation(value = "select", notes = "查询")
    @GetMapping("select")
    @RequiresPermissions("user:select")
    public Object select() {
        return "Select";
    }

    @ApiOperation(value = "create", notes = "新增")
    @GetMapping("create")
    @RequiresPermissions("user:create")
    public Object create() {
        return "Create";
    }

    @ApiOperation(value = "update", notes = "更新")
    @GetMapping("update")
    @RequiresPermissions("user:update")
    public Object update() {
        return "Update";
    }

    @ApiOperation(value = "delete", notes = "删除")
    @GetMapping("delete")
    @RequiresPermissions("user:delete")
    public Object delete() {
        return "Delete";
    }

}
