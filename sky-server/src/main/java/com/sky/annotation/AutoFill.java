package com.sky.annotation;

import com.sky.enumeration.OperationType;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//明确用与方法
@Retention(RetentionPolicy.RUNTIME)//运行时给 AOP 看
public @interface AutoFill {
    OperationType value();
}
