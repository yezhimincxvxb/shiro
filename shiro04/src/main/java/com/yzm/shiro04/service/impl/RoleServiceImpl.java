package com.yzm.shiro04.service.impl;

import com.yzm.shiro04.entity.Role;
import com.yzm.shiro04.mapper.RoleMapper;
import com.yzm.shiro04.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author Yzm
 * @since 2021-10-08
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

}
