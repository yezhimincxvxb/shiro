package com.yzm.shiro09.config;

import com.yzm.common.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Shiro注解模式下，登录失败与没有权限都是通过抛出异常,并且默认并没有去处理或者捕获这些异常。
 * 解决方式，通过自定义全局异常捕获处理
 */
@Slf4j
@RestControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler
    public void ErrorHandler(AuthorizationException e, HttpServletResponse response) throws IOException {
        log.error("异常捕获");
        if (e instanceof UnauthenticatedException) {
            HttpUtils.errorWrite(response, "未登录，请登录");
        } else if (e instanceof UnauthorizedException) {
            HttpUtils.errorWrite(response, "权限不足，禁止访问");
        }
    }

}
