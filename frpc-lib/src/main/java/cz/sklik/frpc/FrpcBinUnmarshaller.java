package cz.sklik.frpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class FrpcBinUnmarshaller {

    private InputStream mInput;

    public FrpcBinUnmarshaller(InputStream in) {
        mInput = in;
    }

    private int read() throws FrpcDataException {
        int data = -1;
        try {
            data = mInput.read();
        } catch (IOException e) {
            throw new FrpcDataException("Error when reading data for unmarschaller: \n" + e);
        }
        if (data == -1) {
            throw new FrpcDataException(
                    "Error when reading data for unmarschaller: No data in input stream\n");
        }
        return data;
    }

    private int read(int offset) throws FrpcDataException {
        int data = -1;
        for (int i = 0; i < offset; i++) {
            data = read();
            if (data == -1) {
                throw new FrpcDataException("Error when reading data for unmarschaller");
            }
        }
        data = read();
        if (data == -1) {
            throw new FrpcDataException("Error when reading data for unmarschaller");

        }
        return data;
    }

    private HashMap<String, Object> unmarshallFault(int data) throws FrpcDataException {
        HashMap<String, Object> fault = new HashMap<String, Object>();
        fault.put("status", unmarshallObject());
        fault.put("statusMessage", unmarshallObject());
        return fault;
    }

    private String unmarshallString(int data) throws FrpcDataException {
        int octets = data & FrpcInternals.MASK_ADD;
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

    private Double unmarshallDouble(int data) throws FrpcDataException {
        long binaryValue = 0;
        for (int i = 0; i < 8; i++) {
            binaryValue |= (long) read() << (i << 3);
        }
        return Double.longBitsToDouble(binaryValue);
    }

    private Long unmarshallInt(int data) throws FrpcDataException {
        int octets = data & FrpcInternals.MASK_ADD;
        long value = 0;

        for (int i = 0; i <= octets; i++) {
            value |= (long) read() << (i << 3);
        }
        return value;
    }

    private Boolean unmarshallBoolean(int data) throws FrpcDataException {
        int octets = data & 1;
        boolean bvalue = octets == 1 ? true : false;
        return bvalue;
    }

    private Object[] unmarshallArray(int data) throws FrpcDataException {
        int octets = data & FrpcInternals.MASK_ADD;
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

    private HashMap<String, Object> unmarshallStruct(int data) throws FrpcDataException {
        int octets = data & FrpcInternals.MASK_ADD;
        int length = 0;
        for (int i = 0; i <= octets; i++) {
            length |= read() << (i << 3);
        }
        HashMap<String, Object> struct = new HashMap<String, Object>();
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
        year += FrpcInternals.DATE_YEAR_OFFSET;

        Calendar datetime = GregorianCalendar.getInstance();

        datetime.setTimeInMillis(unixtimestamp * 1000);

        return datetime;
    }

    private ByteBuffer unmarshallBinary(int data) throws FrpcDataException {
        int octets = data & FrpcInternals.MASK_ADD;
        int length = 0;
        for (int i = 0; i <= octets; i++) {
            length |= read() << (i << 3);
        }
        ByteBuffer binary = null;
        if (length > 0) {
            binary = ByteBuffer.allocate(length);
            for (int i = 0; i < length; i++) {
                binary.put((byte) read());
            }

        }
        return binary;
    }

    public String unmarshallMethodName() throws FrpcDataException {
        int data = read();

        // 0xCA 0x11 = CALL
        if (data == FrpcInternals.MAGIC_NUMBER_FIRST) {
            data = read(3); // major version | minor version | 8+3
        }

        // 01101 000 - method call
        if ((data & FrpcInternals.MASK_TYPE) == FrpcInternals.TYPE_METHOD_CALL) {
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
        Object result = null;
        int data = read();

        if (data == FrpcInternals.MAGIC_NUMBER_FIRST) {
            data = read(3);
        }
        switch (data & FrpcInternals.MASK_TYPE) {
            case FrpcInternals.TYPE_METHOD_RESPONSE:
                result = unmarshallObject();
                break;
            case FrpcInternals.TYPE_FAULT:
                result = unmarshallFault(data);
                break;
            case FrpcInternals.TYPE_STRING:
                result = unmarshallString(data);
                break;
            case FrpcInternals.TYPE_DOUBLE:
                result = unmarshallDouble(data);
                break;
            case FrpcInternals.TYPE_INT_POS:
                result = unmarshallInt(data);
                break;
            case FrpcInternals.TYPE_INT_NEG:
                result = -unmarshallInt(data);
                break;
            case FrpcInternals.TYPE_BOOL:
                result = unmarshallBoolean(data);
                break;
            case FrpcInternals.TYPE_ARRAY:
                result = unmarshallArray(data);
                break;
            case FrpcInternals.TYPE_STRUCT:
                result = unmarshallStruct(data);
                break;
            case FrpcInternals.TYPE_DATETIME:
                result = unmarshallDateTime(data);
                break;
            case FrpcInternals.TYPE_BINARY:
                result = unmarshallBinary(data);
                break;
            case FrpcInternals.TYPE_NULL:
                result = null;
                break;
            default:
                throw new FrpcDataException("Error in unmarshalling: uknown frpc data type! "
                        + (data & FrpcInternals.MASK_TYPE));
        }
        return result;
    }

}
