package com.taskManagement.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    /**
     * 默认构造方法
     */
    public BusinessException() {
        super();
    }

    /**
     * 带消息的构造方法
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 带消息和原因的构造方法
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
} 