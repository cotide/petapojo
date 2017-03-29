package com.viviframework.petapojo.handlers.converters.number;

/**
 * Long/long 类型转换器
 */
public class LongConverter extends NumberConverter<Long> {

    public LongConverter(boolean primitive) {
        super(primitive);
    }

    @Override
    protected Long convertStringValue(String str) {
        return Long.parseLong(str);
    }

    @Override
    protected Long convertNumberValue(Number number) {
        return number.longValue();
    }

    @Override
    protected String getTypeDescription() {
        return Long.class.toString();
    }
}
