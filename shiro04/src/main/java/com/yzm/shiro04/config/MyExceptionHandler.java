//package com.yzm.shiro04.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.shiro.authz.AuthorizationException;
//import org.apache.shiro.authz.UnauthenticatedException;
//import org.apache.shiro.authz.UnauthorizedException;
//import org.apache.shiro.web.util.WebUtils;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * Shiro注解模式下，登录失败与没有权限都是通过抛出异常,并且默认并没有去处理或者捕获这些异常。
// * 解决方式，通过自定义全局异常捕获处理
// */
//@Slf4j
//@RestControllerAdvice
//public class MyExceptionHandler {
//
//    @ExceptionHandler
//    public void ErrorHandler(AuthorizationException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        log.error("注解方式，自定义异常捕获");
//        if (e instanceof UnauthenticatedException) {
//            // 重定向
//            WebUtils.issueRedirect(request, response, "/login");
//        } else if (e instanceof UnauthorizedException) {
//            WebUtils.issueRedirect(request, response, "/401");
//        }
//    }
//
//}
