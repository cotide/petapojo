package com.viviframework.petapojo.handlers.converters.number;

/**
 * Short 类型转换器
 */
public class ShortConverter extends NumberConverter<Short> {

    public ShortConverter(boolean primitive) {
        super(primitive);
    }

    @Override
    protected Short convertStringValue(String str) {
        return Short.parseShort(str);
    }

    @Override
    protected Short convertNumberValue(Number number) {
        return number.shortValue();
    }

    @Override
    protected String getTypeDescription() {
        return Short.class.toString();
    }
}
