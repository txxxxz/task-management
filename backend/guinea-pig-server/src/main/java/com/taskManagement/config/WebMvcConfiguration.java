package com.taskManagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskManagement.interceptor.JwtTokenAdminInterceptor;
import com.taskManagement.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * configure web layer components
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * register custom interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/auth/**", "/api/**", "/tags/**", "/projects/**", "/tasks/**")
                .excludePathPatterns(
                    "/auth/login",
                    "/auth/register",
                    "/auth/check/**",
                    "/auth/logout",
                    "/auth/user/avatar",  // 排除头像上传路径
                    "/api/files/**",      // 排除文件上传路径
                    "/api/tasks/*/attachments"  // 排除任务附件上传路径
                );
    }

    /**
     * 配置跨域支持
     * @param registry
     */
    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        log.info("开始配置跨域支持...");
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)  // 修改为false，因为我们不需要发送凭证
                .maxAge(3600);
    }
    
    /**
     * 添加格式化转换器
     * @param registry
     */
    @Override
    protected void addFormatters(FormatterRegistry registry) {
        log.info("注册LocalDate日期转换器...");
        // 1. 注册ISO标准日期格式转换器
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ISO_DATE); // 设置为ISO标准日期格式 yyyy-MM-dd
        registrar.registerFormatters(registry);
        
        // 2. 添加自定义String到LocalDate转换器，支持多种格式
        registry.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                
                try {
                    // 标准ISO格式 yyyy-MM-dd
                    return LocalDate.parse(source, DateTimeFormatter.ISO_DATE);
                } catch (Exception e1) {
                    try {
                        // 斜杠格式 yyyy/MM/dd
                        return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    } catch (Exception e2) {
                        // 如果依然失败，记录错误并抛出异常
                        log.error("无法将字符串 [{}] 转换为LocalDate", source);
                        throw new IllegalArgumentException("Invalid date format: " + source);
                    }
                }
            }
        });
        
        log.info("LocalDate日期转换器注册完成");
    }
    
    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("Guinea Pig Task Management Project API Documentation")
                .version("2.0")
                .description("Guinea Pig Task Management Project API Documentation")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.taskManagement.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 扩展Spring MVC 框架的消息转换器
     * @param converters
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters){
        log.info("扩展消息转换器……");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        //需要为消息转换器设置注入的ObjectMapper
        converter.setObjectMapper(objectMapper);

        //将自己的消息转换器加入容器中
        converters.add(0,converter);
    }
}
