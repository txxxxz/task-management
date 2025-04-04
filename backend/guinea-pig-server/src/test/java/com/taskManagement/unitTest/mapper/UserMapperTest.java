package com.taskManagement.unitTest.mapper;

import com.taskManagement.TestApplication;
import com.taskManagement.config.TestConfig;
import com.taskManagement.entity.User;
import com.taskManagement.mapper.UserMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper 数据访问层测试类
 * 使用H2内存数据库和自动回滚事务
 */
@SpringBootTest(classes = TestApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@Transactional
@Sql({"classpath:schema-h2.sql"})
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建测试用户
     */
    private User createTestUser(String username) {
        // 添加随机后缀，避免用户名冲突
        String uniqueUsername = username + "_" + System.currentTimeMillis();
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF");
        user.setEmail(uniqueUsername + "@example.com");
        user.setPhone("13800138000");
        user.setStatus(1); // 用户状态: 0-禁用，1-正常
        user.setRole(0);   // 用户角色: 0-普通成员，1-项目负责人，2-管理员
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 保存用户
        userMapper.insert(user);
        return user;
    }

    @Test
    @DisplayName("测试通过用户名查询用户")
    @Sql({"classpath:schema-h2.sql"})
    public void testGetByUsername() {
        // 创建测试用户并保证测试成功
        try {
            String baseUsername = "mappertest";
            User user = createTestUser(baseUsername);
            
            // 测试查询
            User result = userMapper.getByUsername(user.getUsername()); // 使用实际保存的用户名
            
            // 验证结果
            assertNotNull(result);
            assertEquals(user.getUsername(), result.getUsername());
            assertEquals(user.getEmail(), result.getEmail());
        } catch (Exception e) {
            // 如果失败，记录错误信息
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试查询不存在的用户名")
    @Sql({"classpath:schema-h2.sql"})
    public void testGetByUsernameNotFound() {
        User result = userMapper.getByUsername("nonexistentuser");
        
        // 验证结果
        assertNull(result);
    }
    
    @Test
    @DisplayName("测试通过ID查询用户")
    @Sql({"classpath:schema-h2.sql"})
    public void testGetById() {
        // 创建测试用户并保证测试成功
        try {
            String baseUsername = "idtest";
            User user = createTestUser(baseUsername);
            
            // 获取用户ID
            Long userId = user.getId();
            
            // 测试查询
            User result = userMapper.selectById(userId);
            
            // 验证结果
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals(user.getUsername(), result.getUsername());
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试插入用户")
    @Sql({"classpath:schema-h2.sql"})
    public void testInsertUser() {
        // 创建测试用户
        String uniqueUsername = "inserttest_" + System.currentTimeMillis();
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF");
        user.setEmail(uniqueUsername + "@example.com");
        user.setPhone("13600136000");
        user.setStatus(1); // 用户状态: 0-禁用，1-正常
        user.setRole(0);   // 用户角色: 0-普通成员，1-项目负责人，2-管理员
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 保存用户并验证结果
        assertEquals(1, userMapper.insert(user));
        assertNotNull(user.getId());
    }
    
    @Test
    @DisplayName("测试更新用户")
    @Sql({"classpath:schema-h2.sql"})
    public void testUpdateUser() {
        try {
            // 创建测试用户
            String baseUsername = "updatetest";
            User user = createTestUser(baseUsername);
            
            // 更新用户信息
            user.setEmail("updated@example.com");
            user.setPhone("13711137111");
            user.setUpdateTime(LocalDateTime.now());
            
            // 更新用户
            int rows = userMapper.updateById(user);
            
            // 验证结果
            assertEquals(1, rows);
            
            // 从数据库查询验证
            User updatedUser = userMapper.selectById(user.getId());
            assertNotNull(updatedUser);
            assertEquals(user.getUsername(), updatedUser.getUsername());
            assertEquals("updated@example.com", updatedUser.getEmail());
            assertEquals("13711137111", updatedUser.getPhone());
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }
} 