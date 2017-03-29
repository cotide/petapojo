package com.viviframework.petapojo.handlers.converters.number;

/**
 * Integer 类型转换器
 */
public class IntegerConverter extends NumberConverter<Integer> {

    public IntegerConverter(boolean primitive) {
        super(primitive);
    }

    @Override
    protected Integer convertStringValue(String str) {
        return Integer.parseInt(str);
    }

    @Override
    protected Integer convertNumberValue(Number number) {
        return number.intValue();
    }

    @Override
    protected String getTypeDescription() {
        return Integer.class.toString();
    }
}
