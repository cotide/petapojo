package com.viviframework.petapojo.handlers.converters;

/**
 * Boolean/boolean 类型转换器
 */
public class BooleanConverter implements Converter<Boolean> {

    public Boolean convert(Object val) throws ConvertException {
        if (val == null) {
            return null;
        }

        if (val instanceof Boolean) {
            return (Boolean) val;
        }

        if (val instanceof Number) {
            return ((Number) val).intValue() == 0;
        }

        if (val instanceof Character) {
            return (char) val == 'Y'
                    || (char) val == 'T'
                    || (char) val == 'J';
        }

        if (val instanceof String) {
            String str = ((String) val).trim();

            return str.equalsIgnoreCase("Y")
                    || str.equalsIgnoreCase("T")
                    || str.equalsIgnoreCase("J")
                    || str.equalsIgnoreCase("YES")
                    || str.equalsIgnoreCase("TRUE");
        }

        throw new ConvertException("Cannot convert type " + val.getClass().toString() + " to Boolean");
    }
}
