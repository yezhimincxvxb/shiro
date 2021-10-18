//package com.yzm.shiro08.config;
//
//import org.apache.shiro.authz.Authorizer;
//import org.apache.shiro.authz.ModularRealmAuthorizer;
//import org.apache.shiro.realm.Realm;
//import org.apache.shiro.subject.PrincipalCollection;
//
//public class CustomerAuthorizer extends ModularRealmAuthorizer {
//
//    @Override
//    public boolean isPermitted(PrincipalCollection principals, String permission) {
//        this.assertRealmsConfigured();
//
//        for (Realm realm : getRealms()) {
//            if (!(realm instanceof Authorizer)) continue;
//            // 指定自定义的JwtRealm来处理
//            if (realm instanceof JwtRealm) {
//                return ((JwtRealm) realm).isPermitted(principals, permission);
//            }
//        }
//
//        return super.isPermitted(principals, permission);
//    }
//
//    @Override
//    public boolean hasRole(PrincipalCollection principals, String roleIdentifier) {
//        this.assertRealmsConfigured();
//
//        for (Realm realm : getRealms()) {
//            if (!(realm instanceof Authorizer)) continue;
//            // 指定自定义的JwtRealm来处理
//            if (realm instanceof JwtRealm) {
//                return ((JwtRealm) realm).hasRole(principals, roleIdentifier);
//            }
//        }
//
//        return super.hasRole(principals, roleIdentifier);
//    }
//
//
//
//}
