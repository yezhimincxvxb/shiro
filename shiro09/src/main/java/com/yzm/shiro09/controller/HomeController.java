package com.yzm.shiro09.controller;

import com.yzm.common.entity.HttpResult;
import com.yzm.shiro09.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Api(value = "主页信息", tags = {"Home_API"})
public class HomeController {

    @ApiOperation(value = "login", notes = "登录")
    @PostMapping("login")
    public Object doLogin(@RequestParam String username, @RequestParam String password) {
        String token;
        try {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            Subject subject = SecurityUtils.getSubject();
            subject.login(usernamePasswordToken);

            // 生成token
            Map<String, Object> map = new HashMap<>();
            map.put(JwtUtils.USERNAME, username);
            token = JwtUtils.generateToken(map);
        } catch (IncorrectCredentialsException ice) {
            return HttpResult.error("password error!");
        } catch (UnknownAccountException uae) {
            return HttpResult.error("username error!");
        }

        return HttpResult.ok(token);
    }

    @ApiOperation(value = "logout", notes = "退出")
    @GetMapping("/logout")
    public Object logout() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated() || subject.isRemembered()) {
            subject.logout();
        }
        return HttpResult.ok("退出成功");
    }

}
