package com.taskManagement.controller;

import com.taskManagement.result.Result;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.dto.UserUpdateDTO;
import com.taskManagement.dto.PasswordChangeDTO;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.service.UserService;
import com.taskManagement.properties.JwtProperties;
import com.taskManagement.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import javax.validation.Valid;

/**
 * 用户相关接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final JwtProperties jwtProperties;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO);
        return Result.success(userService.login(loginDTO));
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO);
        return Result.success(userService.register(registerDTO));
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/info")
    public Result<UserVO> getCurrentUser(@RequestHeader(value = "${guineapig.jwt.user-token-name}") String token) {
        try {
            log.info("获取用户信息，token: {}", token);
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "无效的token");
            }
            // 从token中获取用户ID
            Long userId = JwtUtil.getUserIdFromToken(jwtProperties.getUserSecretKey(), token);
            // 查询并返回用户信息
            log.info("获取当前用户信息: {}", userId);
            return Result.success(userService.getUserById(userId));
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/check/{username}")
    public Result<Boolean> checkUsername(@PathVariable String username) {
        log.info("检查用户名是否已存在: {}", username);
        return Result.success(userService.checkUsernameExist(username));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        log.info("退出登录");
        return Result.success("退出登录成功");
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/user/info")
    public Result<UserVO> updateUserInfo(@RequestBody UserUpdateDTO updateDTO, 
            @RequestHeader(value = "${guineapig.jwt.user-token-name}") String token) {
        try {
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "无效的token");
            }
            Long userId = JwtUtil.getUserIdFromToken(jwtProperties.getUserSecretKey(), token);
            log.info("更新用户信息: {}", updateDTO);
            return Result.success(userService.updateUserInfo(userId, updateDTO));
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            return Result.error("更新用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/user/password")
    public Result<String> changePassword(@RequestBody PasswordChangeDTO passwordDTO, 
            @RequestHeader(value = "${guineapig.jwt.user-token-name}") String token) {
        try {
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "无效的token");
            }
            Long userId = JwtUtil.getUserIdFromToken(jwtProperties.getUserSecretKey(), token);
            userService.changePassword(userId, passwordDTO);
            log.info("密码修改成功");
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            return Result.error("修改密码失败：" + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/user/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, 
            @RequestParam(value = "userId", required = false) Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            String avatarUrl = userService.uploadAvatar(userId, file);
            log.info("头像上传成功: {}", avatarUrl);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("头像上传失败：", e);
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
} 