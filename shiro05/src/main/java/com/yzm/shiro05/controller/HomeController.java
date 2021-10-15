package com.yzm.shiro05.controller;

import com.yzm.common.entity.HttpResult;
import com.yzm.shiro05.config.MySessionListener;
import com.yzm.shiro05.entity.User;
import com.yzm.shiro05.service.UserService;
import com.yzm.shiro05.utils.EncryptUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated() || subject.isRemembered()) {
            subject.logout();
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }

    @GetMapping("hello")
    @RequiresGuest //登录状态不能访问
    @ResponseBody
    public Object hello() {
        return "hello";
    }

    @GetMapping("list")
    @RequiresUser
    @ResponseBody
    public Object userList() {
        return userService.list();
    }

    @GetMapping("list2")
    @RequiresAuthentication
    @ResponseBody
    public Object userList2() {
        return userService.list();
    }

    @GetMapping("count")
    @ResponseBody
    public Object sessionCount() {
        return MySessionListener.getSessionCount().get();
    }

}
