package cz.sklik.frpc;

class FrpcInternals {

    final static byte TYPE_BOOL = 0x10;

    final static byte TYPE_STRING = 0x20;

    final static byte TYPE_DOUBLE = 0x18;

    final static byte TYPE_DATETIME = 0x28;

    final static byte TYPE_BINARY = 0x30;

    final static byte TYPE_STRUCT = 0x50;

    final static byte TYPE_ARRAY = 0x58;

    final static byte TYPE_METHOD_CALL = 0x68;

    final static byte TYPE_METHOD_RESPONSE = 0x70;

    final static byte TYPE_FAULT = 0x78;

    final static byte TYPE_INT_POS = 0x38;

    final static byte TYPE_INT_NEG = 0x40;

    final static byte TYPE_NULL = 0x60;

    final static int MASK_ADD = 0x7;

    final static int MASK_TYPE = 0xf8;

    final static byte[] MAGIC_NUMBER = {
            (byte) 0xca, 0x11, 0x2, 0x1
    };

    final static int MAGIC_NUMBER_FIRST = 0xca;

    final static int DATE_YEAR_OFFSET = 1600;

}
