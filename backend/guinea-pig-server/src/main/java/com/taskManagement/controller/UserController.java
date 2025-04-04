package com.taskManagement.controller;

import com.taskManagement.result.Result;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.dto.UserUpdateDTO;
import com.taskManagement.dto.PasswordChangeDTO;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import com.taskManagement.service.UserService;
import com.taskManagement.utils.JwtUtil;
import com.taskManagement.properties.JwtProperties;
import com.taskManagement.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import com.taskManagement.vo.PageResult;
import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * 用户管理 - 获取用户列表
     */
    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUserList(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "role", required = false) Integer role,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestHeader(value = "${guineapig.jwt.user-token-name}") String token) {
        try {
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "无效的token");
            }
            
            // 验证用户权限（只有管理员和项目负责人可以查看用户列表）
            Long userId = JwtUtil.getUserIdFromToken(jwtProperties.getUserSecretKey(), token);
            UserVO currentUser = userService.getUserById(userId);
            if (currentUser.getRole() < 1) {
                return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足");
            }
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("email", email);
            params.put("role", role);
            params.put("status", status);
            params.put("page", page);
            params.put("pageSize", pageSize);
            
            // 执行查询
            log.info("获取用户列表: {}", params);
            return Result.success(userService.getUserList(params));
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage());
            return Result.error("获取用户列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户管理 - 创建用户
     */
    @PostMapping("/users")
    public Result<UserVO> createUser(@RequestBody @Valid UserRegisterDTO registerDTO,
                                    @RequestHeader(value = "${guineapig.jwt.user-token-name}") String token) {
        try {
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "无效的token");
            }
            
            // 验证用户权限（只有管理员可以创建用户）
            Long userId = JwtUtil.getUserIdFromToken(jwtProperties.getUserSecretKey(), token);
            UserVO currentUser = userService.getUserById(userId);
            if (currentUser.getRole() < 2) {
                return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足");
            }
            
            // 创建用户
            log.info("创建用户: {}", registerDTO);
            return Result.success(userService.register(registerDTO));
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage());
            return Result.error("创建用户失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户管理 - 更新用户
     */
    @PutMapping("/users/{id}")
    public Result<UserVO> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO updateDTO,
                                    @RequestHeader(value = "${guineapig.jwt.user-token-name}") String token) {
        try {
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "无效的token");
            }
            
            // 验证用户权限（只有管理员可以更新用户）
            Long userId = JwtUtil.getUserIdFromToken(jwtProperties.getUserSecretKey(), token);
            UserVO currentUser = userService.getUserById(userId);
            if (currentUser.getRole() < 2) {
                return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足");
            }
            
            // 更新用户
            log.info("更新用户: id={}, updateDTO={}", id, updateDTO);
            return Result.success(userService.updateUserInfo(id, updateDTO));
        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage());
            return Result.error("更新用户失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户管理 - 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id,
                                    @RequestHeader(value = "${guineapig.jwt.user-token-name}") String token) {
        try {
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "无效的token");
            }
            
            // 验证用户权限（只有管理员可以删除用户）
            Long userId = JwtUtil.getUserIdFromToken(jwtProperties.getUserSecretKey(), token);
            UserVO currentUser = userService.getUserById(userId);
            if (currentUser.getRole() < 2) {
                return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足");
            }
            
            // 删除用户
            log.info("删除用户: id={}", id);
            userService.deleteUser(id);
            return Result.success("删除用户成功");
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return Result.error("删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 用户管理 - 更新用户状态
     */
    @PatchMapping("/users/{id}/status")
    public Result<UserVO> updateUserStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Integer> statusMap,
            @RequestHeader(value = "${guineapig.jwt.user-token-name}") String token) {
        try {
            // 验证token是否有效
            if (!JwtUtil.validateToken(jwtProperties.getUserSecretKey(), token)) {
                return Result.error(HttpStatus.UNAUTHORIZED.value(), "无效的token");
            }
            
            // 验证用户权限（只有管理员可以更新用户状态）
            Long userId = JwtUtil.getUserIdFromToken(jwtProperties.getUserSecretKey(), token);
            UserVO currentUser = userService.getUserById(userId);
            if (currentUser.getRole() < 2) {
                return Result.error(HttpStatus.FORBIDDEN.value(), "权限不足");
            }
            
            // 获取状态值
            Integer status = statusMap.get("status");
            if (status == null || (status != 0 && status != 1)) {
                return Result.error("状态值无效，必须为0或1");
            }
            
            // 没有直接获取User实体和updateById方法，需要使用UserVO和现有方法
            UserVO userVO = userService.getUserById(id);
            if (userVO == null) {
                return Result.error("用户不存在");
            }
            
            // 创建UserUpdateDTO对象并设置参数，使用已有字段
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setUsername(userVO.getUsername());
            updateDTO.setEmail(userVO.getEmail());
            updateDTO.setPhone(userVO.getPhone());
            
            // 调用现有的更新方法
            UserVO updatedUser = userService.updateUserInfo(id, updateDTO);
            
            // 手动设置状态（因为UserUpdateDTO不包含status字段）
            // 注意：这里需要在UserService中添加一个更新状态的方法
            userService.updateUserStatus(id, status);
            
            // 获取并返回更新后的用户信息
            return Result.success(userService.getUserById(id));
        } catch (Exception e) {
            log.error("更新用户状态失败: {}", e.getMessage());
            return Result.error("更新用户状态失败：" + e.getMessage());
        }
    }
} 