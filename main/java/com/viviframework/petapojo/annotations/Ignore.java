package com.viviframework.petapojo.annotations;

import java.lang.annotation.*;

/**
 * 非映射字段注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ignore {
}
