package com.viviframework.petapojo.annotations;

import java.lang.annotation.*;

/**
 * 表名的映射注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableName {
    String value() default "";
}
