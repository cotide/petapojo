package com.viviframework.petapojo.handlers.converters;

import com.viviframework.petapojo.enums.IEnumMessage;
import com.viviframework.petapojo.enums.EnumUtils;

/**
 * 枚举转换器
 */
public class DefaultEnumConverterFactory implements EnumConverterFactory {
    @Override
    public <T extends IEnumMessage> Converter<T> newConverter(final Class<T> enumClass) {
        return new Converter<T>() {
            @Override
            public T convert(Object val) throws ConvertException {
                if (val == null) {
                    return null;
                }

                if(!IEnumMessage.class.isAssignableFrom(enumClass)){
                    throw new ConvertException(String.format("enum [%s] must extend IEnumMessage",enumClass.toString()));
                }

                if(!(val instanceof Number)){
                    throw new ConvertException(String.format("enum value [%s] must bu Number", val.getClass().toString()));
                }

                try {
                    return EnumUtils.getEnum(enumClass,((Number)val).intValue());
                } catch (Throwable e) {
                    throw new ConvertException("connot convert type " + val.getClass().toString() + " to " + enumClass.toString(), e);
                }
            }
        };
    }
}
