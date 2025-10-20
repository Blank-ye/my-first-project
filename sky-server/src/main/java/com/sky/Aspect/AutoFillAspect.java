package com.sky.Aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static com.sky.constant.AutoFillConstant.SET_CREATE_TIME;

@Component
@Slf4j
@Aspect
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut(){}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段的填充..");

        //获取当前拦截的数据库造作类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);//获取方法的注解对象
        OperationType value = annotation.value();//获取注解对象的值


        //获取当前被拦截的方法的参数--实体对像
        Object[] args = joinPoint.getArgs();
        //判断是否存在参数
        if (args == null || args.length== 0){
            return;
        }
        Object arg = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();


        //根据当前不同的数据类型，为对应的属性通过反射对其赋值
        if(value == OperationType.INSERT){
            try {
                Method declaredMethod = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method declaredMethod1 = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method declaredMethod2 = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method declaredMethod3 = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                declaredMethod.invoke(arg, now);
                declaredMethod1.invoke(arg, currentId);
                declaredMethod2.invoke(arg, now);
                declaredMethod3.invoke(arg, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (value==OperationType.UPDATE) {
            try {

                Method declaredMethod2 = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method declaredMethod3 = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                declaredMethod2.invoke(arg, now);
                declaredMethod3.invoke(arg, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }


    }
}
