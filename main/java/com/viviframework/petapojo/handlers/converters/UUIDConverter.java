package com.viviframework.petapojo.handlers.converters;

import java.util.UUID;

/**
 * UUID类型转换器
 */
public class UUIDConverter implements Converter<UUID> {
    @Override
    public UUID convert(Object val) throws ConvertException {
        if (val == null) {
            return null;
        }

        if (val instanceof UUID) {
            return (UUID) val;
        }

        if (val instanceof String) {
            return UUID.fromString((String) val);
        }

        throw new ConvertException("cannot convert type " + val.getClass().toString() + " to " + UUID.class.toString());
    }
}
