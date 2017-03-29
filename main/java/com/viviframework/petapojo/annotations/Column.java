package com.viviframework.petapojo.annotations;

import java.lang.annotation.*;

/**
 * 列名的注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {

    /**
     * 列名
     *
     * @return 列名
     */
    String value() default "";
}
