package com.taskManagement;

import com.aliyun.oss.OSS;
import com.taskManagement.controller.FileController;
import com.taskManagement.service.FileService;
import com.taskManagement.service.impl.FileServiceImpl;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 专门用于测试的应用配置类
 * 排除部分可能导致冲突的组件
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
@EnableTransactionManagement
@ComponentScan(
    basePackages = "com.taskManagement",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            Application.class, // 排除主应用配置
            FileServiceImpl.class, // 排除文件服务实现
            FileController.class, // 排除文件控制器
            com.taskManagement.mapper.CommentMapper.class,
            com.taskManagement.mapper.ProjectAttachmentMapper.class,
            com.taskManagement.mapper.ProjectMapper.class,
            com.taskManagement.mapper.TaskAttachmentMapper.class,
            com.taskManagement.mapper.TaskMapper.class
        })
    }
)
public class TestApplication {

    @MockBean
    private FileService fileService;
    
    /**
     * 提供测试用的OSS客户端模拟
     */
    @Bean
    @Primary
    public OSS ossClient() {
        return Mockito.mock(OSS.class);
    }
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
} 