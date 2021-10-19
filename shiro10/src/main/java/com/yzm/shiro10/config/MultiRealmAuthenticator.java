package com.yzm.shiro10.config;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 自定义多领域认证器
 */
public class MultiRealmAuthenticator extends ModularRealmAuthenticator {

    @Override
    protected AuthenticationInfo doAuthenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
        if (authenticationToken instanceof CustomToken) {
            assertRealmsConfigured();

            CustomToken customToken = (CustomToken) authenticationToken;

            // 登录类型对应的Realm
            Collection<Realm> typeRealms = new ArrayList<>();
            for (Realm realm : getRealms()) {
                if (realm.getName().contains(customToken.getLoginType())) typeRealms.add(realm);
            }

            return typeRealms.size() == 1 ?
                    this.doSingleRealmAuthentication(typeRealms.iterator().next(), authenticationToken) :
                    this.doMultiRealmAuthentication(typeRealms, authenticationToken);
        }
        return super.doAuthenticate(authenticationToken);
    }

}
