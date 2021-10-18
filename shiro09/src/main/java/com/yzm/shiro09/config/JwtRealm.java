package com.yzm.shiro09.config;

import com.yzm.shiro09.entity.JwtToken;
import com.yzm.shiro09.entity.User;
import com.yzm.shiro09.service.UserService;
import com.yzm.shiro09.utils.JwtUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class JwtRealm extends AuthorizingRealm {

    private final UserService userService;

    public JwtRealm(UserService userService) {
        this.userService = userService;
    }

    /**
     * 限定这个Realm只支持我们自定义的JWT Token
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) authToken;
        String token = jwtToken.getToken();

        String username = JwtUtils.getUsernameFromToken(token);
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new UnknownAccountException("账号异常");
        }

        return new SimpleAuthenticationInfo(
                username,
                token,
                getName());
    }

    /**
     * 由于在MyShiroRealm#doGetAuthorizationInfo中以及进行授权了，所以这里可以直接返回空的角色权限
     *
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return new SimpleAuthorizationInfo();
    }

}
