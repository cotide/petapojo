package com.viviframework.petapojo.annotations;

import java.lang.annotation.*;

/**
 * 主键的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrimaryKey {

    /**
     * 主键名
     *
     * @return 主键名
     */
    String value() default "";

    /**
     * 是否自增
     *
     * @return
     */
    boolean autoIncrement() default true;
}
