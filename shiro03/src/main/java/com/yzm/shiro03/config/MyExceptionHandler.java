package com.yzm.shiro03.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 使用注解方式，访问没有权限的接口
 * 是不会被shiroFilterFactoryBean.setUnauthorizedUrl("/403");正确的返回给前端错误信息的，而是直接报错
 * 所以这里自定义异常拦截类
 */
@Slf4j
//@RestControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler
    public void ErrorHandler(AuthorizationException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("注解方式，自定义异常捕获");
        if (e instanceof UnauthenticatedException) {
            WebUtils.issueRedirect(request, response, "/login");
        } else if (e instanceof UnauthorizedException) {
            WebUtils.issueRedirect(request, response, "/401");
        }
    }

}
