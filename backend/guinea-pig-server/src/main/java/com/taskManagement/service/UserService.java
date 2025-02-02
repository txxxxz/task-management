package com.taskManagement.service;

import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;

public interface UserService {
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginVO login(UserLoginDTO loginDTO);

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserVO register(UserRegisterDTO registerDTO);

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    boolean checkUsernameExist(String username);
}
