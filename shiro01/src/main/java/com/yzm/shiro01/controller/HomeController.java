package com.yzm.shiro01.controller;

import com.yzm.common.entity.HttpResult;
import com.yzm.shiro01.entity.User;
import com.yzm.shiro01.service.UserService;
import com.yzm.shiro01.utils.EncryptUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
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
        map.addAttribute("subject", subject);
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
    @ResponseBody
    public Object register(@RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        // 密码加密
        EncryptUtils.encryptPassword(user);
        userService.save(user);
        return HttpResult.ok();
    }

    @PostMapping("login")
    public void doLogin(@RequestParam String username, @RequestParam String password, boolean rememberMe) {
//        try {
        // 1.创建UsernamePasswordToken
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
        if (rememberMe) usernamePasswordToken.setRememberMe(true);
        // 2.创建Subject 用户主体
        Subject subject = SecurityUtils.getSubject();
        // 3.前期准备后，开始登录
        subject.login(usernamePasswordToken);
//        } catch (IncorrectCredentialsException ice) {
//            return HttpResult.error("password error!");
//        } catch (UnknownAccountException uae) {
//            return HttpResult.error("username error!");
//        }
//        return HttpResult.ok();
    }

}
