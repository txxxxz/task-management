package com.taskManagement.integrationTest;

import com.taskManagement.TestApplication;
import com.taskManagement.config.TestConfig;
import com.taskManagement.dto.PasswordChangeDTO;
import com.taskManagement.dto.UserLoginDTO;
import com.taskManagement.dto.UserRegisterDTO;
import com.taskManagement.dto.UserUpdateDTO;
import com.taskManagement.entity.User;
import com.taskManagement.exception.UserBusinessException;
import com.taskManagement.mapper.UserMapper;
import com.taskManagement.service.UserService;
import com.taskManagement.vo.LoginVO;
import com.taskManagement.vo.UserVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务层集成测试
 * 测试 UserService 的各项功能
 */
@SpringBootTest(classes = TestApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    // 测试用户信息
    private static final String TEST_USERNAME = "testServiceUser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "service@example.com";
    private static final String TEST_PHONE = "13812345678";

    // 存储创建的用户ID
    private static Long userId;

    /**
     * 测试用户注册功能
     */
    @Test
    @Order(1)
    @DisplayName("测试UserService注册用户-成功")
    public void testRegisterSuccess() {
        // 准备注册数据
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername(TEST_USERNAME);
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setEmail(TEST_EMAIL);
        registerDTO.setPhone(TEST_PHONE);

        // 执行注册
        UserVO userVO = userService.register(registerDTO);

        // 验证注册结果
        assertThat(userVO).isNotNull();
        assertThat(userVO.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(userVO.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(userVO.getPhone()).isEqualTo(TEST_PHONE);

        // 验证数据库中的记录
        User user = userMapper.getByUsername(TEST_USERNAME);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getPhone()).isEqualTo(TEST_PHONE);

        // 存储用户ID
        userId = user.getId();
    }

    /**
     * 测试用户注册 - 用户名已存在
     */
    @Test
    @Order(2)
    @DisplayName("测试UserService注册用户-用户名已存在")
    public void testRegisterDuplicateUsername() {
        // 先注册一个用户
        testRegisterSuccess();

        // 再次使用相同用户名注册
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername(TEST_USERNAME);
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setEmail("another@example.com");
        registerDTO.setPhone("13900000000");

        // 执行注册，预期抛出异常
        Exception exception = assertThrows(UserBusinessException.class, () -> {
            userService.register(registerDTO);
        });

        // 验证异常信息
        String expectedMessage = "用户名已存在";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * 测试用户登录 - 成功
     */
    @Test
    @Order(3)
    @DisplayName("测试UserService登录-成功")
    public void testLoginSuccess() {
        // 先注册一个用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 准备登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword(TEST_PASSWORD);

        // 执行登录
        LoginVO loginVO = userService.login(loginDTO);

        // 验证登录结果
        assertThat(loginVO).isNotNull();
        assertThat(loginVO.getToken()).isNotNull();
        assertThat(loginVO.getUser()).isNotNull();
        assertThat(loginVO.getUser().getUsername()).isEqualTo(TEST_USERNAME);
    }

    /**
     * 测试用户登录 - 密码错误
     */
    @Test
    @Order(4)
    @DisplayName("测试UserService登录-密码错误")
    public void testLoginWrongPassword() {
        // 先注册一个用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 准备登录数据（错误密码）
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword("wrongpassword");

        // 执行登录，预期抛出异常
        Exception exception = assertThrows(UserBusinessException.class, () -> {
            userService.login(loginDTO);
        });

        // 验证异常信息
        String expectedMessage = "登录失败：密码错误";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * 测试根据ID获取用户信息
     */
    @Test
    @Order(5)
    @DisplayName("测试UserService获取用户信息-成功")
    public void testGetUserById() {
        // 先注册一个用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 获取用户信息
        User user = userMapper.getByUsername(TEST_USERNAME);
        UserVO userVO = userService.getUserById(user.getId());

        // 验证用户信息
        assertThat(userVO).isNotNull();
        assertThat(userVO.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(userVO.getEmail()).isEqualTo(TEST_EMAIL);
    }

    /**
     * 测试更新用户信息
     */
    @Test
    @Order(6)
    @DisplayName("测试UserService更新用户信息-成功")
    public void testUpdateUserInfo() {
        // 先注册一个用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 获取用户ID
        User user = userMapper.getByUsername(TEST_USERNAME);
        Long userId = user.getId();

        // 准备更新数据
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername(TEST_USERNAME); // 保持用户名不变
        updateDTO.setEmail("updated@example.com");
        updateDTO.setPhone("13987654321");

        // 执行更新
        UserVO updatedUser = userService.updateUserInfo(userId, updateDTO);

        // 验证更新结果
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getPhone()).isEqualTo("13987654321");

        // 验证数据库中的记录
        User updatedDbUser = userMapper.getByUsername(TEST_USERNAME);
        assertThat(updatedDbUser).isNotNull();
        assertThat(updatedDbUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedDbUser.getPhone()).isEqualTo("13987654321");
    }

    /**
     * 测试修改密码
     */
    @Test
    @Order(7)
    @DisplayName("测试UserService修改密码-成功")
    public void testChangePassword() {
        // 先注册一个用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 获取用户ID
        User user = userMapper.getByUsername(TEST_USERNAME);
        Long userId = user.getId();

        // 准备修改密码数据
        PasswordChangeDTO passwordDTO = new PasswordChangeDTO();
        passwordDTO.setOldPassword(TEST_PASSWORD);
        passwordDTO.setNewPassword("newpassword123");

        // 执行修改密码
        assertDoesNotThrow(() -> {
            userService.changePassword(userId, passwordDTO);
        });

        // 验证新密码是否能够成功登录
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword("newpassword123");

        LoginVO loginVO = userService.login(loginDTO);
        assertThat(loginVO).isNotNull();
        assertThat(loginVO.getUser().getUsername()).isEqualTo(TEST_USERNAME);
    }

    /**
     * 测试修改密码 - 旧密码错误
     */
    @Test
    @Order(8)
    @DisplayName("测试UserService修改密码-旧密码错误")
    public void testChangePasswordWrongOldPassword() {
        // 先注册一个用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 获取用户ID
        User user = userMapper.getByUsername(TEST_USERNAME);
        Long userId = user.getId();

        // 准备修改密码数据（错误的旧密码）
        PasswordChangeDTO passwordDTO = new PasswordChangeDTO();
        passwordDTO.setOldPassword("wrongoldpassword");
        passwordDTO.setNewPassword("newpassword123");

        // 执行修改密码，预期抛出异常
        Exception exception = assertThrows(UserBusinessException.class, () -> {
            userService.changePassword(userId, passwordDTO);
        });

        // 验证异常信息
        String expectedMessage = "原密码错误";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * 测试删除用户
     */
    @Test
    @Order(9)
    @DisplayName("测试UserService删除用户-成功")
    public void testDeleteUser() {
        // 创建一个用于删除的测试用户
        String deleteUsername = "deleteServiceUser";
        User userToDelete = new User();
        userToDelete.setUsername(deleteUsername);
        userToDelete.setPassword(com.taskManagement.utils.PasswordUtil.encode(TEST_PASSWORD));
        userToDelete.setEmail("deleteservice@example.com");
        userToDelete.setPhone("13900001111");
        userToDelete.setStatus(1);
        userToDelete.setRole(0);
        userMapper.insert(userToDelete);

        // 验证用户是否创建成功
        User createdUser = userMapper.getByUsername(deleteUsername);
        assertThat(createdUser).isNotNull();

        // 执行删除
        assertDoesNotThrow(() -> {
            userService.deleteUser(createdUser.getId());
        });

        // 验证用户是否已被删除
        User deletedUser = userMapper.getByUsername(deleteUsername);
        assertThat(deletedUser).isNull();
    }

    /**
     * 测试更新用户状态
     */
    @Test
    @Order(10)
    @DisplayName("测试UserService更新用户状态-成功")
    public void testUpdateUserStatus() {
        // 先注册一个用户
        if (userMapper.getByUsername(TEST_USERNAME) == null) {
            testRegisterSuccess();
        }

        // 获取用户ID
        User user = userMapper.getByUsername(TEST_USERNAME);
        Long userId = user.getId();

        // 执行状态更新（禁用用户）
        boolean result = userService.updateUserStatus(userId, 0);
        assertTrue(result);

        // 验证数据库中的状态
        User updatedUser = userMapper.getByUsername(TEST_USERNAME);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getStatus()).isEqualTo(0);

        // 执行状态更新（启用用户）
        result = userService.updateUserStatus(userId, 1);
        assertTrue(result);

        // 验证数据库中的状态
        updatedUser = userMapper.getByUsername(TEST_USERNAME);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getStatus()).isEqualTo(1);
    }
} 