package com.viviframework.petapojo.handlers.converters;

import com.viviframework.petapojo.enums.IEnumMessage;

/**
 * 枚举转换工厂
 */
public interface EnumConverterFactory {

    <T extends IEnumMessage> Converter<T> newConverter(Class<T> enumClass);
}
