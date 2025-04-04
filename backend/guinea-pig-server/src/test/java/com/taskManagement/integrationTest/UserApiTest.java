package com.taskManagement.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskManagement.TestApplication;
import com.taskManagement.config.TestConfig;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.entity.User;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.result.Result;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户API集成测试类
 * 专注于测试基本的用户功能接口，如注册、登录等
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    // 测试用户信息
    private static final String TEST_USERNAME = "testApiUser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "testapi@example.com";
    private static final String TEST_PHONE = "13812345678";

    /**
     * 测试用户注册功能
     * 验证用户注册接口能正确接收请求并创建用户
     */
    @Test
    @Order(1)
    @DisplayName("测试用户注册API-成功")
    public void testRegisterSuccess() throws Exception {
        // 准备注册数据
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername(TEST_USERNAME);
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setEmail(TEST_EMAIL);
        registerDTO.setPhone(TEST_PHONE);

        // 执行注册请求
        MvcResult result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(1)))
                .andExpect(jsonPath("$.data.username", is(TEST_USERNAME)))
                .andExpect(jsonPath("$.data.email", is(TEST_EMAIL)))
                .andExpect(jsonPath("$.data.phone", is(TEST_PHONE)))
                .andReturn();

        // 验证数据库中是否正确保存了用户数据
        User user = userMapper.getByUsername(TEST_USERNAME);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getPhone()).isEqualTo(TEST_PHONE);
    }

    /**
     * 测试用户注册时用户名已存在的情况
     */
    @Test
    @Order(2)
    @DisplayName("测试用户注册API-用户名已存在")
    public void testRegisterDuplicateUsername() throws Exception {
        // 先注册一个用户
        testRegisterSuccess();

        // 再次使用相同用户名注册
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername(TEST_USERNAME);
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setEmail("another@example.com");
        registerDTO.setPhone("13900000000");

        // 执行注册请求，预期失败
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0))); // 失败代码为0
    }

    /**
     * 测试用户登录功能
     */
    @Test
    @Order(3)
    @DisplayName("测试用户登录API-成功")
    public void testLoginSuccess() throws Exception {
        // 先确保有用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 准备登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword(TEST_PASSWORD);

        // 执行登录请求
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(1)))
                .andExpect(jsonPath("$.data.user.username", is(TEST_USERNAME)))
                .andExpect(jsonPath("$.data.token").exists());
    }

    /**
     * 测试用户登录时密码错误的情况
     */
    @Test
    @Order(4)
    @DisplayName("测试用户登录API-密码错误")
    public void testLoginWrongPassword() throws Exception {
        // 先确保有用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 准备登录数据（错误密码）
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword("wrongpassword");

        // 执行登录请求，预期失败
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0))); // 失败代码为0
    }

    /**
     * 测试参数校验 - 用户名为空
     */
    @Test
    @Order(5)
    @DisplayName("测试参数校验API-用户名为空")
    public void testValidationEmptyUsername() throws Exception {
        // 准备数据（缺少用户名）
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setEmail(TEST_EMAIL);
        registerDTO.setPhone(TEST_PHONE);

        // 执行请求，预期失败并返回400
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试参数校验 - 邮箱格式错误
     */
    @Test
    @Order(6)
    @DisplayName("测试参数校验API-邮箱格式错误")
    public void testValidationInvalidEmail() throws Exception {
        // 准备数据（无效邮箱格式）
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setEmail("invalidEmail");
        registerDTO.setPhone(TEST_PHONE);

        // 执行请求，预期失败并返回400
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }
} 