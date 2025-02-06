package com.taskManagement.service;

import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.dto.UserUpdateDTO;
import com.taskManagement.dto.PasswordChangeDTO;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 更新用户信息
     * @param userId
     * @param updateDTO
     * @return
     */
    public UserVO updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    /**
     * 修改密码
     * @param userId
     * @param passwordDTO
     */
    public void changePassword(Long userId, PasswordChangeDTO passwordDTO);

    /**
     * 上传头像
     * @param userId
     * @param file
     * @return
     */
    public String uploadAvatar(Long userId, MultipartFile file);
}
