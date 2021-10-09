package com.yzm.shiro01.service.impl;

import com.yzm.shiro01.entity.Role;
import com.yzm.shiro01.mapper.RoleMapper;
import com.yzm.shiro01.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author Yzm
 * @since 2021-10-07
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

}
