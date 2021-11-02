package com.yzm.shiro10.controller;

import com.yzm.shiro10.config.CustomToken;
import com.yzm.shiro10.config.LoginType;
import com.yzm.shiro10.entity.User;
import com.yzm.shiro10.service.UserService;
import com.yzm.shiro10.utils.EncryptUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        UsernamePasswordToken token;
        if ("admin".equals(username)) {
            token = new CustomToken(username, password, LoginType.ADMIN.type());
        } else {
            token = new CustomToken(username, password, LoginType.USER.type());
        }
        token.setRememberMe(rememberMe);

        String url = "/home";
        try {
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);
        } catch (IncorrectCredentialsException e) {
            url = "/login?failure";
        } catch (UnknownAccountException e) {
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

}
