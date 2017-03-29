package com.viviframework.petapojo.handlers.converters.number;

/**
 * Byte 类型转换器
 */
public class ByteConverter extends NumberConverter<Byte> {

    public ByteConverter(boolean primitive) {
        super(primitive);
    }

    @Override
    protected Byte convertStringValue(String str) {
        return Byte.parseByte(str);
    }

    @Override
    protected Byte convertNumberValue(Number number) {
        return number.byteValue();
    }

    @Override
    protected String getTypeDescription() {
        return Byte.class.toString();
    }
}
