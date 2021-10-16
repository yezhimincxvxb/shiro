package com.yzm.shiro07.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzm.shiro07.entity.Permissions;
import com.yzm.shiro07.mapper.PermissionsMapper;
import com.yzm.shiro07.service.PermissionsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 权限表 服务实现类
 * </p>
 *
 * @author Yzm
 * @since 2021-10-09
 */
@Service
public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions> implements PermissionsService {

}
