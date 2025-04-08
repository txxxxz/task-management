package com.taskManagement.handler;

import com.taskManagement.constant.MessageConstant;
import com.taskManagement.exception.BaseException;
import com.taskManagement.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * global exception handler, handle business exceptions in the project
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * catch business exception
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("exception message: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * handle sql exception
     */

    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //重复键值对
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            String msg=username+  MessageConstant.ALREADY_EXISITS;
            return Result.error(msg);

        }
        else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
