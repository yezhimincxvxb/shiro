package com.yzm.shiro09.config;

import com.yzm.shiro09.utils.JwtUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

/**
 * 校验token是否过期
 */
    public class JwtCredentialsMatcher implements CredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken authenticationToken, AuthenticationInfo authenticationInfo) {
        if (authenticationToken instanceof JwtToken) {
            String token = (String) authenticationInfo.getCredentials();
            return !JwtUtils.isExpired(token);
        }
        return false;
    }

}
