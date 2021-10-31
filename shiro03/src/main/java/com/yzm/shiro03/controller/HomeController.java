package com.yzm.shiro03.controller;

import com.yzm.shiro03.entity.User;
import com.yzm.shiro03.service.UserService;
import com.yzm.shiro03.utils.EncryptUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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

    @GetMapping(value = {"/", "/home"})
    public String home(ModelMap map) {
        Subject subject = SecurityUtils.getSubject();
        map.addAttribute("subject", subject.getPrincipals());
        return "home";
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("401")
    public Object notRole() {
        return "401";
    }

    @PostMapping("register")
    public Object register(ModelMap map, @RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        // 密码加密
        EncryptUtils.encryptPassword(user);
        userService.save(user);
        map.addAttribute("user", user);
        return "home";
    }

    @PostMapping("login")
    public Object login(@RequestParam String username, @RequestParam String password, boolean rememberMe) {
        // 1.创建UsernamePasswordToken
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
        // 使用记住我功能
        usernamePasswordToken.setRememberMe(rememberMe);
        // 2.创建Subject 用户主体
        Subject subject = SecurityUtils.getSubject();

        String url = "/home";
        try {
            // 3.前期准备后，开始登录
            subject.login(usernamePasswordToken);
        } catch (IncorrectCredentialsException e) {
            url = "/login?failure";
        } catch (AuthenticationException e) {
            url = "/login";
        }
        return "redirect:" + url;
    }

    @PostMapping("/logout")
    public Object logout() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated() || subject.isRemembered()) {
            subject.logout();
        }
        return "redirect:/login";
    }

    @GetMapping("requiresGuest")
    @RequiresGuest
    @ResponseBody
    public Object requiresGuest() {
        return "requiresGuest";
    }

    @GetMapping("requiresAuthentication")
    @RequiresAuthentication
    @ResponseBody
    public Object requiresAuthentication() {
        return "requiresAuthentication";
    }

    @GetMapping("requiresUser")
    @RequiresUser
    @ResponseBody
    public Object requiresUser() {
        return "requiresUser";
    }

    @GetMapping("/fail")
    @ResponseBody
    public Object fail() {
        int i = 1 / 0;
        return "出错了";
    }

    @GetMapping("404")
    public Object notFound() {
        return "404";
    }

    @GetMapping("500")
    public Object error() {
        return "500";
    }


}
