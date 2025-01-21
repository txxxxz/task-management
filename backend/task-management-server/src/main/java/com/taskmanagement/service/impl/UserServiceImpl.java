package com.taskmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskmanagement.mapper.UserMapper;
import com.taskmanagement.entity.User;
import com.taskmanagement.dto.LoginDTO;
import com.taskmanagement.vo.LoginVO;
import com.taskmanagement.service.UserService;
import com.taskmanagement.utils.JwtUtil;
import com.taskmanagement.utils.MD5Util;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public User register(User user) {
        // 1. 对密码进行 MD5 加密
        String encryptedPassword = MD5Util.encrypt(user.getPassword());
        user.setPassword(encryptedPassword);
        
        // 2. 保存用户
        this.save(user);
        
        return user;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 对密码进行 MD5 加密
        String encryptedPassword = MD5Util.encrypt(loginDTO.getPassword());
        
        // 2. 根据用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(queryWrapper);
        
        // 3. 验证用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 4. 验证密码是否正确
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new RuntimeException("密码错误");
        }
        
        // 5. 生成 JWT token
        String token = JwtUtil.generateToken(user.getId());
        
        // 6. 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(user);
        
        return loginVO;
    }
} 