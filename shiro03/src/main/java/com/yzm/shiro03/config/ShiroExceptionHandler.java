package com.yzm.shiro03.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 使用注解方式，访问没有权限的接口
 * 是不会被shiroFilterFactoryBean.setUnauthorizedUrl("/403");正确的返回给前端错误信息的，而是直接报错
 * 所以这里自定义异常拦截类
 */
@Slf4j
//@RestControllerAdvice
public class ShiroExceptionHandler {

    @ExceptionHandler
    public String ErrorHandler(AuthorizationException e) {
        log.error("没有访问权限！");
        return "没有访问权限！";
    }

}
