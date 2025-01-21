package com.taskmanagement.service;

import com.taskmanagement.dto.LoginDTO;
import com.taskmanagement.vo.LoginVO;
import com.taskmanagement.vo.UserVO;

public interface UserService {
    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册成功的用户信息
     */
    UserVO register(UserVO user);

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO);
} 