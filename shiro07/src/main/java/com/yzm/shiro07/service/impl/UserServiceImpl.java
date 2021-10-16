package com.yzm.shiro07.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzm.shiro07.entity.User;
import com.yzm.shiro07.mapper.UserMapper;
import com.yzm.shiro07.service.UserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Yzm
 * @since 2021-10-09
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
