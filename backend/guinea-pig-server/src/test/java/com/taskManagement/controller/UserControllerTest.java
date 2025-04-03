package com.taskManagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskManagement.TestApplication;
import com.taskManagement.config.TestConfig;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.service.UserService;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;
    
    // 所有其他相关依赖也需要Mock
    @MockBean
    private com.taskManagement.properties.JwtProperties jwtProperties;
    
    @MockBean
    private com.taskManagement.utils.JwtUtil jwtUtil;

    @Test
    @DisplayName("用户登录成功测试")
    public void testLoginSuccess() throws Exception {
        // 准备测试数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
        
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("testuser");
        userVO.setEmail("test@example.com");
        userVO.setStatus(1);
        userVO.setRole(0);
        
        LoginVO loginVO = LoginVO.builder()
                .token("test-token")
                .user(userVO)
                .build();
        
        // 模拟Service层
        when(userService.login(any(UserLoginDTO.class))).thenReturn(loginVO);
        
        // 执行请求并验证结果
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(1)))
                .andExpect(jsonPath("$.msg", nullValue()))
                .andExpect(jsonPath("$.data.token", is("test-token")))
                .andExpect(jsonPath("$.data.user.username", is("testuser")));
    }

    @Test
    @DisplayName("用户注册成功测试")
    public void testRegisterSuccess() throws Exception {
        // 准备测试数据
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("new@example.com");
        registerDTO.setPhone("13812345678");
        
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("newuser");
        userVO.setEmail("new@example.com");
        userVO.setPhone("13812345678");
        userVO.setStatus(1);
        userVO.setRole(0);
        
        // 模拟Service层
        when(userService.register(any(UserRegisterDTO.class))).thenReturn(userVO);
        
        // 执行请求并验证结果
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(1)))
                .andExpect(jsonPath("$.msg", nullValue()))
                .andExpect(jsonPath("$.data.username", is("newuser")))
                .andExpect(jsonPath("$.data.email", is("new@example.com")));
    }
} 