package com.yzm.shiro04.utils;

import com.yzm.shiro04.config.MySimpleByteSource;
import com.yzm.shiro04.entity.User;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;

/**
 * 加密
 * 注册用户时加密密码
 * 认证的时候，校验密码正确性
 */
public class EncryptUtils {

    private static final RandomNumberGenerator randomNumberGenerator = new SecureRandomNumberGenerator();
    public static final String ALGORITHM_NAME = "MD5"; // 散列算法
    public static final int HASH_ITERATIONS = 2; // 散列次数

    public static void encryptPassword(User user) {
        // 随机字符串作为salt因子，实际参与运算的salt我们还引入其它干扰因子
        user.setSalt(randomNumberGenerator.nextBytes().toHex());
        user.setPassword(new SimpleHash(
                ALGORITHM_NAME,
                user.getPassword(),
                // 用户名 + 盐
                // 这里可以换，也可以不用
                // ByteSource.Util.bytes(user.getUsername() + user.getSalt()),
                new MySimpleByteSource(user.getUsername() + user.getSalt()),
                HASH_ITERATIONS
        ).toHex());
    }

}
