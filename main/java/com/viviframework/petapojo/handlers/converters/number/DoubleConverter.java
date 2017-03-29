package com.viviframework.petapojo.handlers.converters.number;

/**
 * Double 类型转换器
 */
public class DoubleConverter extends NumberConverter<Double> {

    public DoubleConverter(boolean primitive) {
        super(primitive);
    }

    @Override
    protected Double convertStringValue(String str) {
        return Double.parseDouble(str);
    }

    @Override
    protected Double convertNumberValue(Number number) {
        return number.doubleValue();
    }

    @Override
    protected String getTypeDescription() {
        return Double.class.toString();
    }
}
