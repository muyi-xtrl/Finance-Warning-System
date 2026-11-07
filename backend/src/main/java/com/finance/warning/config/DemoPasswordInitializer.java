package com.finance.warning.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.warning.entity.SysUser;
import com.finance.warning.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 演示环境：确保默认账号密码为 123456（BCrypt）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoPasswordInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        String encoded = passwordEncoder.encode("123456");
        for (String username : new String[]{"admin", "analyst", "viewer"}) {
            SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username));
            if (user != null && !passwordEncoder.matches("123456", user.getPassword())) {
                user.setPassword(encoded);
                userMapper.updateById(user);
                log.info("已重置演示账号 {} 密码为 123456", username);
            }
        }
    }
}
