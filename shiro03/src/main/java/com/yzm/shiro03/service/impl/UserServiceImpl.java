package com.yzm.shiro03.service.impl;

import com.yzm.shiro03.entity.User;
import com.yzm.shiro03.mapper.UserMapper;
import com.yzm.shiro03.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Yzm
 * @since 2021-10-08
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
