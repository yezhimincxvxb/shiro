package com.yzm.shiro02.controller;


import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping
    public Object user() {
        return SecurityUtils.getSubject().getPrincipal();
    }

    @GetMapping("select")
    public Object select() {
        return "Select";
    }

    @GetMapping("create")
    public Object create() {
        return "Create";
    }

    @GetMapping("update")
    public Object update() {
        return "Update";
    }

    @GetMapping("delete")
    public Object delete() {
        return "Delete";
    }

    @GetMapping("createAndUpdate")
    public Object createAndUpdate() {
        return "Create And Update";
    }

}
