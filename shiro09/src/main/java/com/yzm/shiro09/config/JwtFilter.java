package com.yzm.shiro09.config;

import com.alibaba.fastjson.JSONObject;
import com.yzm.common.entity.HttpResult;
import com.yzm.shiro09.entity.JwtToken;
import com.yzm.shiro09.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtFilter extends AuthenticatingFilter {

    /**
     * 跨域支持
     * 前置处理
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * 跨域支持
     * 后置处理
     */
    @Override
    protected void postHandle(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
    }

    /**
     * 父类会在请求进入拦截器后调用该方法，返回true则继续，返回false则会调用onAccessDenied()。
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        boolean allowed = false;
        try {
            allowed = executeLogin(request, response);
        } catch (IllegalStateException e) {
            log.error("Not found any token");
        } catch (Exception e) {
            log.error("Error occurs when login", e);
        }
        return allowed || super.isPermissive(mappedValue);
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        AuthenticationToken token = this.createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        } else {
            try {
                Subject subject = this.getSubject(request, response);
                subject.login(token);
                return this.onLoginSuccess(token, subject, request, response);
            } catch (AuthenticationException var5) {
                return this.onLoginFailure(token, var5, request, response);
            }
        }
    }

    /**
     * 这里重写了父类的方法，使用我们自己定义的Token类，提交给shiro。这个方法返回null的话会直接抛出异常，进入isAccessAllowed（）的异常处理逻辑。
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String jwtToken = JwtUtils.getTokenFromRequest(WebUtils.toHttp(request));
        if (StringUtils.isNotBlank(jwtToken) && !JwtUtils.isExpired(jwtToken))
            return new JwtToken(jwtToken);
        return null;
    }

    /**
     * 如果这个Filter在之前isAccessAllowed（）方法中返回false,则会进入这个方法。我们这里直接返回错误的response
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        HttpResult result = new HttpResult(401, "抱歉，你未经授权", null);
        response.getWriter().print(JSONObject.toJSONString(result, true));
        response.getWriter().flush();
        response.getWriter().close();
        return false;
    }

    /**
     * 如果Shiro Login认证成功，会进入该方法，等同于用户名密码登录成功，我们这里还判断了是否要刷新Token
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        log.info("token认证成功");
        if (token instanceof JwtToken) {
            JwtToken jwtToken = (JwtToken) token;
            String tokenStr = jwtToken.getToken();
            if (JwtUtils.isExpired(tokenStr)) {
                String username = (String) subject.getPrincipal();
                Map<String, Object> map = new HashMap<>();
                map.put(JwtUtils.USERNAME, username);
                tokenStr = JwtUtils.generateToken(map);
            }
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setHeader("token", tokenStr);
        }
        return true;
    }

    /**
     * 如果调用shiro的login认证失败，会回调这个方法
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        log.info("token认证失败");
        if (e instanceof IncorrectCredentialsException) {
            log.error("凭证错误，token过期");
            JwtToken jwtToken = (JwtToken) token;
            String tokenStr = jwtToken.getToken();
            String username = JwtUtils.getUsernameFromToken(tokenStr);

            Map<String, Object> map = new HashMap<>();
            map.put(JwtUtils.USERNAME, username);
            String newToken = JwtUtils.generateToken(map);
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setHeader("token", newToken);
        }
        if (e instanceof UnknownAccountException) {
            log.error("账号异常");
        }
        return false;
    }


}
