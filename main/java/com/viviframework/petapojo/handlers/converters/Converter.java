package com.viviframework.petapojo.handlers.converters;

/**
 * 类型转换接口
 */
public interface Converter<T> {

    T convert(Object val) throws ConvertException;
}
