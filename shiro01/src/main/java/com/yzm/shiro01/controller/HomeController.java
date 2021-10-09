package com.yzm.shiro01.controller;

import com.yzm.common.entity.HttpResult;
import com.yzm.shiro01.entity.User;
import com.yzm.shiro01.service.UserService;
import com.yzm.shiro01.utils.EncryptUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("home")
    public Object home() {
        return "home";
    }

    @GetMapping("401")
    public Object notRole() {
        return "401";
    }

    @PostMapping("register")
    @ResponseBody
    public Object register(@RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        EncryptUtils.encryptPassword(user);
        userService.save(user);
        return HttpResult.ok();
    }

    @PostMapping("doLogin")
    @ResponseBody
    public Object doLogin(@RequestParam String username, @RequestParam String password, boolean rememberMe) {
        try {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            if (rememberMe) usernamePasswordToken.setRememberMe(true);
            Subject subject = SecurityUtils.getSubject();
            subject.login(usernamePasswordToken);
        } catch (IncorrectCredentialsException ice) {
            return HttpResult.error("password error!");
        } catch (UnknownAccountException uae) {
            return HttpResult.error("username error!");
        }
        return HttpResult.ok();
    }

}
