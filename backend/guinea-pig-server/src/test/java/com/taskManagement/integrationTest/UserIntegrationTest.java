package com.taskManagement.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskManagement.TestApplication;
import com.taskManagement.config.TestConfig;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.entity.User;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.properties.JwtProperties;
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
 * 用户功能集成测试
 * 测试用户的注册、登录功能
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    // 测试用户信息
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PHONE = "13812345678";
    
    // 管理员信息
    private static final String ADMIN_USERNAME = "adminUser";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PHONE = "13800000000";
    
    // 存储登录后获取的token
    private static String userToken;
    private static String adminToken;
    
    // 存储注册的用户ID
    private static Long userId;
    private static Long adminId;
    
    // 存储测试用户对象和管理员对象，供后续测试使用
    private static User testUser;
    private static User adminUser;

    /**
     * 初始化测试环境
     * 创建一个普通用户和一个管理员用户
     */
    @BeforeEach
    public void setupBefore() {
        if (userMapper.getByUsername(ADMIN_USERNAME) == null) {
            // 创建管理员用户
            User admin = new User();
            admin.setUsername(ADMIN_USERNAME);
            admin.setPassword(com.taskManagement.utils.PasswordUtil.encode(ADMIN_PASSWORD));
            admin.setEmail(ADMIN_EMAIL);
            admin.setPhone(ADMIN_PHONE);
            admin.setStatus(1); // 正常
            admin.setRole(2);   // 管理员角色
            userMapper.insert(admin);
            adminUser = admin;
        } else {
            adminUser = userMapper.getByUsername(ADMIN_USERNAME);
        }
        
        // 确保有一个普通用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            User user = new User();
            user.setUsername(TEST_USERNAME);
            user.setPassword(com.taskManagement.utils.PasswordUtil.encode(TEST_PASSWORD));
            user.setEmail(TEST_EMAIL);
            user.setPhone(TEST_PHONE);
            user.setStatus(1); // 正常
            user.setRole(0);   // 普通用户角色
            userMapper.insert(user);
            testUser = user;
        } else {
            testUser = userMapper.getByUsername(TEST_USERNAME);
        }
    }

    @Test
    @Order(1)
    @DisplayName("测试用户注册-成功")
    public void testRegisterSuccess() throws Exception {
        // 准备注册数据，使用随机用户名以避免冲突
        String username = "new" + System.currentTimeMillis();
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setEmail(TEST_EMAIL);
        registerDTO.setPhone(TEST_PHONE);

        // 执行注册请求
        MvcResult result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(1)))
                .andExpect(jsonPath("$.data.username", is(username)))
                .andExpect(jsonPath("$.data.email", is(TEST_EMAIL)))
                .andExpect(jsonPath("$.data.phone", is(TEST_PHONE)))
                .andReturn();

        // 从响应中获取用户ID
        String responseContent = result.getResponse().getContentAsString();
        Result response = objectMapper.readValue(responseContent, Result.class);
        userId = Long.valueOf(((java.util.LinkedHashMap) response.getData()).get("id").toString());

        // 验证数据库中是否正确保存了用户数据
        User user = userMapper.getByUsername(username);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getPhone()).isEqualTo(TEST_PHONE);
    }

    @Test
    @Order(2)
    @DisplayName("测试用户注册-用户名已存在")
    public void testRegisterDuplicateUsername() throws Exception {
        // 使用已存在的用户名进行注册
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
                .andExpect(jsonPath("$.code", is(0)));  // 失败代码为0
    }

    @Test
    @Order(3)
    @DisplayName("测试用户登录-成功")
    public void testLoginSuccess() throws Exception {
        // 准备登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword(TEST_PASSWORD);

        // 执行登录请求
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(1)))
                .andExpect(jsonPath("$.data.user.username", is(TEST_USERNAME)))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        // 保存token用于后续测试
        String responseContent = result.getResponse().getContentAsString();
        Result response = objectMapper.readValue(responseContent, Result.class);
        userToken = ((java.util.LinkedHashMap) response.getData()).get("token").toString();
        
        // 更新用户ID
        userId = testUser.getId();
    }
    
    @Test
    @Order(4)
    @DisplayName("测试管理员登录-成功")
    public void testAdminLoginSuccess() throws Exception {
        // 准备登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(ADMIN_USERNAME);
        loginDTO.setPassword(ADMIN_PASSWORD);
        loginDTO.setRole(2); // 管理员角色

        // 执行登录请求
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(1)))
                .andExpect(jsonPath("$.data.user.username", is(ADMIN_USERNAME)))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        // 保存token用于后续测试
        String responseContent = result.getResponse().getContentAsString();
        Result response = objectMapper.readValue(responseContent, Result.class);
        adminToken = ((java.util.LinkedHashMap) response.getData()).get("token").toString();
        
        // 更新管理员ID
        adminId = adminUser.getId();
    }

    @Test
    @Order(5)
    @DisplayName("测试用户登录-密码错误")
    public void testLoginWrongPassword() throws Exception {
        // 准备登录数据（错误密码）
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword("wrongpassword");

        // 执行登录请求，预期失败
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));  // 失败代码为0
    }

    @Test
    @Order(6)
    @DisplayName("测试参数校验-用户名为空")
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

    @Test
    @Order(7)
    @DisplayName("测试参数校验-邮箱格式错误")
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