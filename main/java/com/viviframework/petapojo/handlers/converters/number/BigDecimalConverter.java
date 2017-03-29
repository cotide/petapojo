package com.viviframework.petapojo.handlers.converters.number;

import java.math.BigDecimal;

/**
 * BigDecimal类型转换器
 */
public class BigDecimalConverter extends NumberConverter<BigDecimal> {

    public BigDecimalConverter() {
        super(false);
    }

    @Override
    protected BigDecimal convertStringValue(String str) {
        return BigDecimal.valueOf(Double.parseDouble(str));
    }

    @Override
    protected BigDecimal convertNumberValue(Number number) {
        return BigDecimal.valueOf(number.doubleValue());
    }

    @Override
    protected String getTypeDescription() {
        return BigDecimal.class.toString();
    }
}
