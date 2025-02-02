package com.taskManagement.controller.user;

import com.taskManagement.result.Result;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        // TODO: 从token中获取用户ID，然后查询用户信息
        return Result.success(null);
    }
    
    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/check/{username}")
    public Result<Boolean> checkUsername(@PathVariable String username) {
        return Result.success(userService.checkUsernameExist(username));
    }
} 