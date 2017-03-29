package com.viviframework.petapojo.handlers.converters.number;

/**
 * Float类型转换器
 */
public class FloatConverter extends NumberConverter<Float> {

    public FloatConverter(boolean primitive) {
        super(primitive);
    }

    @Override
    protected Float convertStringValue(String str) {
        return Float.parseFloat(str);
    }

    @Override
    protected Float convertNumberValue(Number number) {
        return number.floatValue();
    }

    @Override
    protected String getTypeDescription() {
        return Float.class.toString();
    }
}
