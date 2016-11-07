package cz.seznam.frpc.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcUnmarshaller {

    private InputStream input;

    public FrpcUnmarshaller(byte[] data) {
        this(new ByteArrayInputStream(Objects.requireNonNull(data)));
    }

    public FrpcUnmarshaller(InputStream inputStream) {
        input = Objects.requireNonNull(inputStream);
    }

    private int read() throws FrpcDataException {
        int data;
        try {
            data = input.read();
        } catch (IOException e) {
            throw new FrpcDataException("Error when reading data for unmarshaller: \n" + e);
        }
        if (data == -1) {
            throw new FrpcDataException(
                    "Error when reading data for unmarshaller: No data in input stream\n");
        }
        return data;
    }

    private int read(int offset) throws FrpcDataException {
        int data;
        for (int i = 0; i < offset; i++) {
            data = read();
            if (data == -1) {
                throw new FrpcDataException("Error when reading data for unmarshaller");
            }
        }
        data = read();
        if (data == -1) {
            throw new FrpcDataException("Error when reading data for unmarshaller");

        }
        return data;
    }

    private Map<String, Object> unmarshallFault(int data) throws FrpcDataException {
        Map<String, Object> fault = new HashMap<>();
        fault.put("status", unmarshallObject());
        fault.put("statusMessage", unmarshallObject());
        return fault;
    }

    private String unmarshallString(int data) throws FrpcDataException {
        int octets = data & FrpcConstants.MASK_ADD;
        int length = 0;
        for (int i = 0; i <= octets; i++) {
            length |= read() << (i << 3);
        }
        ByteBuffer buf = ByteBuffer.allocate(length);
        for (int i = 0; i < length; i++) {
            int c = read();
            buf.put((byte) c);
        }
        String string;
        try {
            string = new String(buf.array(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            string = "Chyba pri enkodovani";
        }
        return string;
    }

    private Number unmarshallFloatingPointType(int data) throws FrpcDataException {
        long binaryValue = 0;
        for (int i = 0; i < 8; i++) {
            binaryValue |= (long) read() << (i << 3);
        }
        double value = Double.longBitsToDouble(binaryValue);
        // if the result fits into float, return float instead
        if((float) value == value) {
            return (float) value;
        } else {
            return value;
        }
    }

    private Number unmarshallIntegralType(int data, boolean positive) throws FrpcDataException {
        int octets = data & FrpcConstants.MASK_ADD;
        long value = 0;

        for (int i = 0; i <= octets; i++) {
            value |= (long) read() << (i << 3);
        }
        // if the name should be negative, make it so
        if(!positive) {
            value = -value;
        }
        // if the name fits into int, return int instead
        if((int) value == value) {
            return (int) value;
        } else {
            return value;
        }
    }

    private Boolean unmarshallBoolean(int data) throws FrpcDataException {
        int octets = data & 1;
        return octets == 1;
    }

    private Object[] unmarshallArray(int data) throws FrpcDataException {
        int octets = data & FrpcConstants.MASK_ADD;
        int length = 0;
        for (int i = 0; i <= octets; i++) {
            length |= read() << (i << 3);
        }
        Object[] array = new Object[length];
        for (int i = 0; i < length; i++) {
            array[i] = unmarshallObject();
        }
        return array;
    }

    private Map<String, Object> unmarshallStruct(int data) throws FrpcDataException {
        int octets = data & FrpcConstants.MASK_ADD;
        int length = 0;
        for (int i = 0; i <= octets; i++) {
            length |= read() << (i << 3);
        }
        Map<String, Object> struct = new HashMap<>();
        for (int i = 0; i < length; i++) {
            int nameLength = read();
            ByteBuffer name = ByteBuffer.allocate(nameLength);
            for (int j = 0; j < nameLength; j++) {
                name.put((byte) read());
            }
            String key;
            try {
                key = new String(name.array(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                key = "Chyba pri enkodovani";
            }
            struct.put(key, unmarshallObject());
        }
        return struct;
    }

    private Calendar unmarshallDateTime(int data) throws FrpcDataException {
        int zone; // (8b)
        long unixtimestamp = 0; // (32b)
        int weekday; // (3b)
        int sec; // (6b)
        int min; // (6b)
        int hour; // (5b)
        int day; // (5b)
        int month; // (4b)
        int year; // (11b)

        zone = read();

        for (int i = 0; i <= 3; i++) {
            unixtimestamp |= read() << (i << 3);
        }

        data = read();
        weekday = (data & 0x07);
        sec = ((data & 0xf8) >> 3);

        data = read();
        sec |= ((data & 0x01) << 5);
        min = ((data & 0x7e) >> 1);
        hour = ((data & 0x80) >> 7);

        data = read();
        hour |= ((data & 0x0f) << 1);
        day = (((data & 0xf0) >> 4));

        data = read();
        day |= ((data & 0x01) << 4);
        month = (data & 0x1e) >> 1;
        year = (data & 0xe0) >> 5;

        data = read();
        year |= ((data << 3));
        year += FrpcConstants.DATE_YEAR_OFFSET;

        Calendar datetime = Calendar.getInstance();

        datetime.setTimeInMillis(unixtimestamp * 1000);

        return datetime;
    }

    private byte[] unmarshallBinary(int data) throws FrpcDataException {
        int octets = data & FrpcConstants.MASK_ADD;
        int length = 0;
        for (int i = 0; i <= octets; i++) {
            length |= read() << (i << 3);
        }
        byte[] binary = new byte[length];
        for (int i = 0; i < length; i++) {
            binary[i] = (byte) read();
        }

        return binary;
    }

    public String unmarshallMethodName() throws FrpcDataException {
        int data = read();

        // 0xCA 0x11 = CALL
        if (data == FrpcConstants.MAGIC_NUMBER_FIRST) {
            data = read(3); // major version | minor version | 8+3
        }

        // 01101 000 - method call
        if ((data & FrpcConstants.MASK_TYPE) == FrpcConstants.TYPE_METHOD_CALL) {
            data = read(); // name size
            return readMethodName(data);
        }

        throw new FrpcDataException("Unable to decode method name");
    }

    private String readMethodName(int length) throws FrpcDataException {
        byte[] arr = new byte[length];
        for (int i = 0; i < length; ++i) {
            arr[i] = (byte) read();
        }
        return new String(arr);
    }

    public Object unmarshallObject() throws FrpcDataException {
        Object result;
        int data = read();

        if (data == FrpcConstants.MAGIC_NUMBER_FIRST) {
            data = read(3);
        }
        switch (data & FrpcConstants.MASK_TYPE) {
            case FrpcConstants.TYPE_METHOD_RESPONSE:
                result = unmarshallObject();
                break;
            case FrpcConstants.TYPE_FAULT:
                result = unmarshallFault(data);
                break;
            case FrpcConstants.TYPE_STRING:
                result = unmarshallString(data);
                break;
            case FrpcConstants.TYPE_DOUBLE:
                result = unmarshallFloatingPointType(data);
                break;
            case FrpcConstants.TYPE_INT_POS:
                result = unmarshallIntegralType(data, true);
                break;
            case FrpcConstants.TYPE_INT_NEG:
                result = unmarshallIntegralType(data, false);
                break;
            case FrpcConstants.TYPE_BOOL:
                result = unmarshallBoolean(data);
                break;
            case FrpcConstants.TYPE_ARRAY:
                result = unmarshallArray(data);
                break;
            case FrpcConstants.TYPE_STRUCT:
                result = unmarshallStruct(data);
                break;
            case FrpcConstants.TYPE_DATETIME:
                result = unmarshallDateTime(data);
                break;
            case FrpcConstants.TYPE_BINARY:
                result = unmarshallBinary(data);
                break;
            case FrpcConstants.TYPE_NULL:
                result = null;
                break;
            default:
                throw new FrpcDataException("Error in unmarshalling: uknown frpc data type! "
                        + (data & FrpcConstants.MASK_TYPE));
        }
        return result;
    }

}
