package com.yzm.shiro10.config;

import com.yzm.shiro10.entity.Permissions;
import com.yzm.shiro10.entity.Role;
import com.yzm.shiro10.entity.User;
import com.yzm.shiro10.service.PermissionsService;
import com.yzm.shiro10.service.RoleService;
import com.yzm.shiro10.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义UserRealm
 */
@Slf4j
public class UserRealm extends AuthorizingRealm {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionsService permissionsService;

    public UserRealm(UserService userService, RoleService roleService, PermissionsService permissionsService) {
        // 设置Realm名称
        setName("UserRealm");
        this.userService = userService;
        this.roleService = roleService;
        this.permissionsService = permissionsService;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof CustomToken;
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        log.info("User 授权");
        String username = (String) principalCollection.getPrimaryPrincipal();
        // 查询用户，获取角色ids
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        List<Integer> roleIds = Arrays.stream(user.getRIds().split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // 查询角色，获取角色名、权限ids
        List<Role> roles = roleService.listByIds(roleIds);
        Set<String> roleNames = new HashSet<>(roles.size());
        Set<Integer> permIds = new HashSet<>();
        roles.forEach(role -> {
            roleNames.add(role.getRName());
            Set<Integer> collect = Arrays.stream(
                    role.getPIds().split(",")).map(Integer::parseInt).collect(Collectors.toSet());
            permIds.addAll(collect);
        });

        // 获取权限名称
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
        log.info("User 认证");
        // 获取用户名跟密码
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        String username = usernamePasswordToken.getUsername();

        // 查询用户是否存在
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new UnknownAccountException();
        }

        return new SimpleAuthenticationInfo(
                user.getUsername(),
                user.getPassword(),
                // 用户名 + 盐
                ByteSource.Util.bytes(user.getUsername() + user.getSalt()),
                getName()
        );
    }
}
