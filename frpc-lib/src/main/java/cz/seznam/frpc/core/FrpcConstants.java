package cz.seznam.frpc.core;

public class FrpcConstants {

    public static final byte TYPE_BOOL = 0x10;

    public static final byte TYPE_STRING = 0x20;

    public static final byte TYPE_DOUBLE = 0x18;

    public static final byte TYPE_DATETIME = 0x28;

    public static final byte TYPE_BINARY = 0x30;

    public static final byte TYPE_STRUCT = 0x50;

    public static final byte TYPE_ARRAY = 0x58;

    public static final byte TYPE_METHOD_CALL = 0x68;

    public static final byte TYPE_METHOD_RESPONSE = 0x70;

    public static final byte TYPE_FAULT = 0x78;

    public static final byte TYPE_INT_POS = 0x38;

    public static final byte TYPE_INT_NEG = 0x40;

    public static final byte TYPE_NULL = 0x60;

    public static final int MASK_ADD = 0x7;

    public static final int MASK_TYPE = 0xf8;

    public static final byte[] MAGIC_NUMBER = {
            (byte) 0xca, 0x11, 0x2, 0x1
    };

    public static final int MAGIC_NUMBER_FIRST = 0xca;

    public static final int DATE_YEAR_OFFSET = 1600;

}
