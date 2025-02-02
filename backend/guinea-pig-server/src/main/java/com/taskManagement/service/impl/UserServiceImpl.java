package com.taskManagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskManagement.exception.UserBusinessException;
import com.taskManagement.utils.JwtUtil;
import com.taskManagement.utils.PasswordUtil;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.entity.User;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final UserMapper userMapper;
    
    @Override
    public LoginVO login(UserLoginDTO userLoginDTO) {
        try {
            // 1. 根据用户名查询用户
            User user = userMapper.getByUsername(userLoginDTO.getUsername());
            if (user == null) {
                throw new UserBusinessException("登录失败：用户名不存在");
            }
            
            // 2. 校验密码
            if (!PasswordUtil.matches(userLoginDTO.getPassword(), user.getPassword())) {
                throw new UserBusinessException("登录失败：密码错误");
            }
            
            // 3. 校验用户状态
            if (user.getStatus() == 0) {
                throw new UserBusinessException("登录失败：账号已被禁用");
            }

            // 4. 校验用户角色
            if (userLoginDTO.getRole() != null && !userLoginDTO.getRole().equals(user.getRole())) {
                String roleDesc = "";
                switch(user.getRole()) {
                    case 0: roleDesc = "普通成员"; break;
                    case 1: roleDesc = "项目负责人"; break;
                    case 2: roleDesc = "管理员"; break;
                    default: roleDesc = "未知角色"; break;
                }
                throw new UserBusinessException(String.format("登录失败：用户角色不匹配。该账号的角色是：%s", roleDesc));
            }
            
            // 5. 生成token
            String secretKey = "guineapig";
            long ttlMillis = 24 * 60 * 60 * 1000; // 24小时
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRole());
            String token = JwtUtil.createJWT(secretKey, ttlMillis, claims);
            
            // 6. 转换并返回数据
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            
            return LoginVO.builder()
                    .token(token)
                    .user(userVO)
                    .build();
        } catch (Exception e) {
            if (e instanceof UserBusinessException) {
                throw e;
            }
            throw new UserBusinessException("登录失败：" + e.getMessage());
        }
    }
    
    @Override
    public UserVO register(UserRegisterDTO registerDTO) {
        // 1. 校验用户名是否已存在
        if (this.checkUsernameExist(registerDTO.getUsername())) {
            throw new UserBusinessException("用户名已存在");
        }
        
        // 2. 创建用户实体并保存
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        // 设置默认值
        user.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        user.setStatus(1);  // 正常状态
        user.setRole(0);    // 设置默认角色为普通成员
        
        this.save(user);
        
        // 3. 转换并返回数据
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
    
    @Override
    public UserVO getUserById(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new UserBusinessException("用户不存在");
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
    
    @Override
    public boolean checkUsernameExist(String username) {
        User user = userMapper.getByUsername(username);
        return user != null;
    }
}
