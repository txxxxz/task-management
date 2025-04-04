package com.taskManagement.unitTest.service;

import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.entity.User;
import com.taskManagement.exception.UserBusinessException;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.service.FileService;
import com.taskManagement.service.UserService;
import com.taskManagement.service.impl.UserServiceImpl;
import com.taskManagement.utils.JwtUtil;
import com.taskManagement.utils.PasswordUtil;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService单元测试 - 纯单元测试方式
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileService fileService;
    
    @Mock
    private com.taskManagement.properties.JwtProperties jwtProperties;

    @InjectMocks
    private UserServiceImpl userService;
    
    @BeforeEach
    public void setup() {
        // 使用反射直接设置baseMapper字段，而不是通过模拟getBaseMapper方法
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    @DisplayName("用户登录成功测试")
    public void testLoginSuccess() {
        // 准备测试数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
        
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$aBcDeFgHiJkLmNoPqRsTuVwXyZ"); 
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setRole(0);
        
        // 设置JWT属性
        when(jwtProperties.getUserSecretKey()).thenReturn("testSecretKey");
        when(jwtProperties.getUserTtl()).thenReturn(7200000L);
        
        // 模拟Mapper层返回
        when(userMapper.getByUsername(loginDTO.getUsername())).thenReturn(testUser);
        
        // 模拟密码匹配和JWT创建，两者都是静态方法
        try (MockedStatic<PasswordUtil> passwordUtil = Mockito.mockStatic(PasswordUtil.class);
             MockedStatic<JwtUtil> jwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            
            // 模拟密码匹配返回true
            passwordUtil.when(() -> PasswordUtil.matches(anyString(), anyString())).thenReturn(true);
            
            // 模拟JWT静态方法createJWT
            jwtUtil.when(() -> JwtUtil.createJWT(anyString(), anyLong(), any())).thenReturn("test-token");
            
            // 执行测试
            LoginVO result = userService.login(loginDTO);
            
            // 验证结果
            assertNotNull(result);
            assertNotNull(result.getUser());
            assertEquals(testUser.getUsername(), result.getUser().getUsername());
        }
    }
    
    @Test
    @DisplayName("用户注册成功测试")
    public void testRegisterSuccess() {
        // 准备测试数据
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("new@example.com");
        registerDTO.setPhone("13812345678");
        
        // 模拟用户名不存在
        when(userMapper.getByUsername(registerDTO.getUsername())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        
        // 模拟密码加密
        try (MockedStatic<PasswordUtil> passwordUtil = Mockito.mockStatic(PasswordUtil.class)) {
            passwordUtil.when(() -> PasswordUtil.encode(anyString())).thenReturn("encrypted_password");
            
            // 执行测试
            UserVO result = userService.register(registerDTO);
            
            // 验证结果
            assertNotNull(result);
            assertEquals(registerDTO.getUsername(), result.getUsername());
            assertEquals(registerDTO.getEmail(), result.getEmail());
        }
    }
    
    @Test
    @DisplayName("用户注册失败测试 - 用户名已存在")
    public void testRegisterFailureUsernameExists() {
        // 准备测试数据
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("existinguser");
        registerDTO.setPassword("password123");
        
        // 模拟用户名已存在
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        when(userMapper.getByUsername(registerDTO.getUsername())).thenReturn(existingUser);
        
        // 验证异常
        UserBusinessException exception = assertThrows(UserBusinessException.class, () -> {
            userService.register(registerDTO);
        });
        
        assertEquals("用户名已存在", exception.getMessage());
    }
} 