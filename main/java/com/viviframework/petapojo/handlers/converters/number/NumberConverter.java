package com.viviframework.petapojo.handlers.converters.number;

import com.viviframework.petapojo.handlers.converters.ConvertException;
import com.viviframework.petapojo.handlers.converters.Converter;

/**
 * Number 类型转换器基类
 */
public abstract class NumberConverter<T extends Number> implements Converter<T> {

    private boolean isPrimitive;

    public NumberConverter(boolean primitive) {
        isPrimitive = primitive;
    }

    public T convert(Object val) throws ConvertException {
        if (val == null) {
            return isPrimitive ? convertNumberValue(0) : null;
        } else if (val instanceof Number) {
            return convertNumberValue((Number) val);
        } else if (val instanceof String) {
            String str = ((String) val).trim();
            str = str.isEmpty() ? null : str;

            if (str == null)
                return isPrimitive ? convertNumberValue(0) : null;

            return convertStringValue(str);
        }

        throw new ConvertException("Cannot convert type " + val.getClass().toString() + " to " + getTypeDescription());
    }

    protected abstract T convertStringValue(String str);

    protected abstract T convertNumberValue(Number number);

    protected abstract String getTypeDescription();
}
