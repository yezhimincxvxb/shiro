package com.yzm.shiro10.config;

import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

/**
 * 自定义多领域授权器
 */
public class MultiRealmAuthorizer extends ModularRealmAuthorizer {

    @Override
    public boolean hasRole(PrincipalCollection principals, String roleIdentifier) {
        assertRealmsConfigured();

        //获取realm的名字
        Set<String> realmNames = principals.getRealmNames();
        String realmName = realmNames.iterator().next();
        for (Realm realm : getRealms()) {
            if (!(realm instanceof Authorizer)) continue;
            // admin
            if (realmName.contains(LoginType.ADMIN.type()) && realm instanceof AdminRealm) {
                return ((AdminRealm) realm).hasRole(principals, roleIdentifier);
            }
            // user
            if (realmName.contains(LoginType.USER.type()) && realm instanceof UserRealm) {
                return ((UserRealm) realm).hasRole(principals, roleIdentifier);
            }
        }
        return false;
    }


    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        assertRealmsConfigured();

        //获取realm的名字
        Set<String> realmNames = principals.getRealmNames();
        String realmName = realmNames.iterator().next();
        for (Realm realm : getRealms()) {
            if (!(realm instanceof Authorizer)) continue;
            // admin
            if (realmName.contains(LoginType.ADMIN.type()) && realm instanceof AdminRealm) {
                return ((AdminRealm) realm).isPermitted(principals, permission);
            }
            // user
            if (realmName.contains(LoginType.USER.type()) && realm instanceof UserRealm) {
                return ((UserRealm) realm).isPermitted(principals, permission);
            }
        }
        return false;
    }


}
