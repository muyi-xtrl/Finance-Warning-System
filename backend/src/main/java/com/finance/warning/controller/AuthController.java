package com.finance.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.warning.common.BusinessException;
import com.finance.warning.common.Result;
import com.finance.warning.dto.LoginRequest;
import com.finance.warning.entity.SysUser;
import com.finance.warning.mapper.SysUserMapper;
import com.finance.warning.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(401, "用户不存在或已禁用");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        return Result.ok(data);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        String username = jwtUtil.parse(token).getSubject();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        return Result.ok(data);
    }
}
