package com.taskManagement.controller.user;

import com.taskManagement.result.Result;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.dto.UserUpdateDTO;
import com.taskManagement.dto.PasswordChangeDTO;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.taskManagement.utils.JwtUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/info")
    public Result<UserVO> getCurrentUser(@RequestHeader("Authorization") String token) {
        // 从token中获取用户ID
        Long userId = JwtUtil.getUserIdFromToken(token);
        // 查询并返回用户信息
        return Result.success(userService.getUserById(userId));
    }
    
    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/check/{username}")
    public Result<Boolean> checkUsername(@PathVariable String username) {
        return Result.success(userService.checkUsernameExist(username));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出登录成功");
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/user/info")
    public Result<UserVO> updateUserInfo(@RequestBody UserUpdateDTO updateDTO, @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        return Result.success(userService.updateUserInfo(userId, updateDTO));
    }

    /**
     * 修改密码
     */
    @PutMapping("/user/password")
    public Result<String> changePassword(@RequestBody PasswordChangeDTO passwordDTO, @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        userService.changePassword(userId, passwordDTO);
        return Result.success("密码修改成功");
    }

    /**
     * 上传头像
     */
    @PostMapping("/user/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success(avatarUrl);
    }
} 