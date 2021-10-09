package com.yzm.shiro03.service.impl;

import com.yzm.shiro03.entity.Permissions;
import com.yzm.shiro03.mapper.PermissionsMapper;
import com.yzm.shiro03.service.PermissionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 权限表 服务实现类
 * </p>
 *
 * @author Yzm
 * @since 2021-10-08
 */
@Service
public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions> implements PermissionsService {

}
