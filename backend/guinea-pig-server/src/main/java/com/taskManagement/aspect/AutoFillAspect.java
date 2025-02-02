package com.taskManagement.aspect;

import com.taskManagement.annotation.AutoFill;
import com.taskManagement.constant.AutoFillConstant;
import com.taskManagement.context.BaseContext;
import com.taskManagement.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     *  切入点：mapper下所有文件的所有方法 AND UPDATE和Insert类
     */
    @Pointcut("execution(* com.taskManagement.mapper.*.*(..)) && @annotation(com.taskManagement.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段的赋值
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段的自动填充");

        //获取当前被拦截的方法上的数据库操作类型
        MethodSignature signature =(MethodSignature) joinPoint.getSignature(); //方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); //获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库的操作类型

        //获取当前被拦截的方法的参数 ———— 实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }

        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now=LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();


        //根据当前不同的操作类型，为对应的属性通过反射赋值（有无create）
        if(operationType==OperationType.INSERT)
        {
            try {
                //为更新操作赋值(4个)
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalArgumentException |
                     IllegalAccessException | SecurityException e) {
                log.error("自动填充字段失败，实体类：{}，异常信息：{}", entity.getClass().getName(), e.getMessage());
                throw new RuntimeException("自动填充字段失败", e);

            }
        }
        else if (operationType==OperationType.UPDATE)
        {
            try {
                //为新增操作赋值（2个）
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException e) {
                if (log.isErrorEnabled()) {
                    log.error("自动填充字段失败，实体类：{}，异常信息：{}", entity.getClass().getName(), e.getMessage());
                }
                throw new RuntimeException("自动填充字段失败", e);
            }
        }
    }
}
