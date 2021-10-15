package com.yzm.shiro05.config;


import com.yzm.shiro05.entity.Permissions;
import com.yzm.shiro05.entity.Role;
import com.yzm.shiro05.entity.User;
import com.yzm.shiro05.service.PermissionsService;
import com.yzm.shiro05.service.RoleService;
import com.yzm.shiro05.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyShiroRealm extends AuthorizingRealm {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionsService permissionsService;

    public MyShiroRealm(UserService userService, RoleService roleService, PermissionsService permissionsService) {
        this.userService = userService;
        this.roleService = roleService;
        this.permissionsService = permissionsService;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String username = (String) principalCollection.getPrimaryPrincipal();
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        List<Integer> roleIds = Arrays.stream(user.getRIds().split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        List<Role> roles = roleService.listByIds(roleIds);
        Set<String> roleNames = new HashSet<>(roles.size());
        Set<Integer> permIds = new HashSet<>();
        roles.forEach(role -> {
            roleNames.add(role.getRName());
            Set<Integer> collect = Arrays.stream(
                    role.getPIds().split(",")).map(Integer::parseInt).collect(Collectors.toSet());
            permIds.addAll(collect);
        });

        List<Permissions> permissions = permissionsService.listByIds(permIds);
        List<String> permNames = permissions.stream().map(Permissions::getPName).collect(Collectors.toList());

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.addRoles(roleNames);
        authorizationInfo.addStringPermissions(permNames);
        return authorizationInfo;
    }

    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        String username = usernamePasswordToken.getUsername();
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new UnknownAccountException();
        }

        return new SimpleAuthenticationInfo(
                user.getUsername(),
                user.getPassword(),
                new MySimpleByteSource(user.getUsername() + user.getSalt()),
                getName()
        );
    }

    /**
     * 重写方法,清除当前用户的的 授权缓存
     */
    @Override
    public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
    }

    /**
     * 重写方法，清除当前用户的 认证缓存
     */
    @Override
    public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }

    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

}
