package com.taskManagement.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置类，用于配置日期时间的序列化和反序列化
 */
@Configuration
public class JacksonConfig {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 设置一些基本配置
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // 创建Java 8日期时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 支持多种日期时间格式的反序列化
        // 优先使用ISO格式，因为前端通常会发送ISO格式的日期时间
        DateTimeDeserializers localDateTimeDeserializers = new DateTimeDeserializers();
        javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializers);
        
        // 在序列化时使用标准格式
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
        
        // 配置LocalDate的序列化和反序列化
        javaTimeModule.addSerializer(LocalDate.class, 
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, 
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        
        // 注册模块
        objectMapper.registerModule(javaTimeModule);
                
        return objectMapper;
    }
    
    /**
     * 自定义LocalDateTime反序列化器，支持多种日期时间格式
     */
    public static class DateTimeDeserializers extends LocalDateTimeDeserializer {
        private final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT);
        private final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
        
        public DateTimeDeserializers() {
            super(DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT));
        }
        
        @Override
        public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) 
                throws java.io.IOException {
            String text = p.getText().trim();
            
            // 尝试ISO格式 (yyyy-MM-dd'T'HH:mm:ss)
            try {
                return LocalDateTime.parse(text, ISO_FORMATTER);
            } catch (Exception e) {
                // 如果ISO格式解析失败，尝试标准格式 (yyyy-MM-dd HH:mm:ss)
                try {
                    return LocalDateTime.parse(text, DEFAULT_FORMATTER);
                } catch (Exception e2) {
                    // 如果都失败，尝试使用ISO_DATE_TIME标准解析器
                    return LocalDateTime.parse(text);
                }
            }
        }
    }
} 