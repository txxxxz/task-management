package com.taskManagement.config;

import com.aliyun.oss.OSS;
import com.taskManagement.Application;
import com.taskManagement.config.AliyunOSSConfig;
import com.taskManagement.config.OSSConfiguration;
import com.taskManagement.config.WebMvcConfiguration;
import com.taskManagement.interceptor.JwtTokenUserInterceptor;
import com.taskManagement.interceptor.JwtTokenAdminInterceptor;
import com.taskManagement.mapper.CommentMapper;
import com.taskManagement.mapper.ProjectAttachmentMapper;
import com.taskManagement.mapper.TaskAttachmentMapper;
import com.taskManagement.mapper.ProjectMapper;
import com.taskManagement.mapper.TaskMapper;
import com.taskManagement.properties.JwtProperties;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.apache.ibatis.session.SqlSessionFactory;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.io.IOException;

@TestConfiguration
@EnableTransactionManagement
@MapperScan(basePackages = "com.taskManagement.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
@ComponentScan(
    basePackages = "com.taskManagement",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                OSSConfiguration.class, 
                AliyunOSSConfig.class, 
                Application.class,
                WebMvcConfiguration.class, // 排除生产环境的WebMvc配置，避免JWT拦截器加载
                JwtTokenUserInterceptor.class, // 排除JWT拦截器
                JwtTokenAdminInterceptor.class, // 排除JWT管理员拦截器
                // 排除所有会导致Bean冲突的Mapper
                CommentMapper.class,
                ProjectAttachmentMapper.class,
                TaskAttachmentMapper.class,
                ProjectMapper.class,
                TaskMapper.class
            }
        ),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
    }
)
public class TestConfig {
    
    @MockBean
    private OSSConfiguration ossConfiguration;
    
    @MockBean
    private AliyunOSSConfig aliyunOSSConfig;
    
    /**
     * 模拟JWT属性配置
     */
    @Bean
    @Primary
    public JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setUserSecretKey("test-secret-key");
        properties.setUserTokenName("Authorization");
        properties.setUserTtl(7200000L);
        properties.setAdminSecretKey("admin-test-secret-key");
        properties.setAdminTokenName("Admin-Authorization");
        properties.setAdminTtl(7200000L);
        properties.setTokenHeader("token");
        return properties;
    }
    
    /**
     * 配置测试环境的MVC设置，使用模拟JWT拦截器
     */
    @Bean
    public WebMvcConfigurer testWebMvcConfigurer(MockJwtTokenUserInterceptor mockJwtTokenUserInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // 使用模拟JWT拦截器，不验证令牌直接通过认证
                registry.addInterceptor(mockJwtTokenUserInterceptor)
                        .addPathPatterns("/**") // 拦截所有请求
                        .excludePathPatterns("/auth/login", "/auth/register", "/error") // 排除登录和注册接口
                        .order(Integer.MIN_VALUE); // 确保这个拦截器最先执行
            }
        };
    }
    
    /**
     * 测试环境安全配置，允许所有请求
     */
    @Configuration
    @Order(1)
    public static class TestSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();
        }
    }
    
    /**
     * 提供OSS客户端模拟
     */
    @Bean
    @Primary
    public OSS ossClient() {
        return Mockito.mock(OSS.class);
    }
    
    /**
     * 提供内存数据库数据源
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema-h2.sql")
            .setName("testdb-" + System.currentTimeMillis()) // 生成唯一数据库名
            .build();
    }
    
    /**
     * 提供事务管理器
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    
    /**
     * 提供SqlSessionFactory配置
     */
    @Bean
    @Primary
    public MybatisSqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws IOException {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        
        // 设置MyBatis配置
        com.baomidou.mybatisplus.core.MybatisConfiguration configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCallSettersOnNulls(true);
        configuration.setCacheEnabled(false);
        sqlSessionFactoryBean.setConfiguration(configuration);
        
        // 设置Mapper XML文件位置
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath*:mapper/*.xml"));
        
        // 设置别名包
        sqlSessionFactoryBean.setTypeAliasesPackage("com.taskManagement.entity");
        
        // 设置MyBatis-Plus全局配置
        com.baomidou.mybatisplus.core.config.GlobalConfig globalConfig = new com.baomidou.mybatisplus.core.config.GlobalConfig();
        com.baomidou.mybatisplus.core.config.GlobalConfig.DbConfig dbConfig = new com.baomidou.mybatisplus.core.config.GlobalConfig.DbConfig();
        dbConfig.setTablePrefix("tb_");  // 设置表前缀
        dbConfig.setIdType(com.baomidou.mybatisplus.annotation.IdType.AUTO); // 设置ID自动生成
        globalConfig.setDbConfig(dbConfig);
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        
        return sqlSessionFactoryBean;
    }
    
    /**
     * 提供SqlSessionTemplate
     */
    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            // 配置MyBatis-Plus使用tb_user作为表名而不是user
            configuration.setMapUnderscoreToCamelCase(true);
            // 开启日志
            configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        };
    }
} 