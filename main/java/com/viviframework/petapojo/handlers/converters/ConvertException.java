package com.viviframework.petapojo.handlers.converters;

/**
 * 类型转换异常类
 */
public class ConvertException extends Exception {

    public ConvertException(String message) {
        super(message);
    }

    public ConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}
