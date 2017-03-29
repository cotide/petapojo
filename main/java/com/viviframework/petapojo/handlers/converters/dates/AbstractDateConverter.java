package com.viviframework.petapojo.handlers.converters.dates;


import com.viviframework.petapojo.handlers.converters.ConvertException;
import com.viviframework.petapojo.handlers.converters.Converter;

import java.util.Date;

/**
 * 时间转换器基类
 */
public abstract class AbstractDateConverter<T extends Date> implements Converter<T> {

    private final Class<T> classOfDate;

    protected AbstractDateConverter(Class<T> classOfDate) {
        this.classOfDate = classOfDate;
    }

    public T convert(Object val) throws ConvertException {
        if (val == null)
            return null;

        if (classOfDate.isInstance(val))
            return (T) val;

        if (val instanceof Date)
            return fromMilliseconds(((Date) val).getTime());

        if (val instanceof Number)
            return fromMilliseconds(((Number) val).longValue());

        throw new ConvertException("Cannot convert type " + val.getClass().toString() + " to " + classOfDate.toString());
    }

    protected abstract T fromMilliseconds(long time);
}
