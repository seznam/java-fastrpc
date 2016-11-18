package cz.seznam.frpc.core.deserialization;

import cz.seznam.frpc.core.FrpcConstants;
import cz.seznam.frpc.core.FrpcDataException;
import cz.seznam.frpc.core.transport.FrpcFault;
import cz.seznam.frpc.core.transport.FrpcRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcUnmarshaller {

    protected InputStream input;

    private static final Object NO_MORE_OBJECTS = new Object();

    public FrpcUnmarshaller(InputStream inputStream) {
        input = Objects.requireNonNull(inputStream, "Input stream must not be null");
    }

    public FrpcRequest readRequest() {
        // read magic number
        readMagic();
        // read method name
        String methodName = readMethodCall();
        // read parameters
        List<Object> parameters = new ArrayList<>();
        while(true) {
            Object parameter = readObject();
            if(parameter != NO_MORE_OBJECTS) {
                parameters.add(parameter);
            } else {
                break;
            }
        }
        // create the request
        return new FrpcRequest(methodName, parameters);
    }

    public Object readResponse() {
        // read magic number
        readMagic();
        // make sure that we are reading method response
        int data = read();
        if((data & FrpcConstants.MASK) != FrpcConstants.TYPE_METHOD_RESPONSE) {
            throw new FrpcDataException("The stream does not contain properly formed method response");
        }
        // read single object
        Object response = readObject();
        // check if it's not the NO_MORE_OBJECTS marker
        if(response == NO_MORE_OBJECTS) {
            throw new FrpcDataException("The stream does not contain any response value");
        }
        // return the response
        return response;
    }

    private void readMagic() {
        try {
            // 0xCA 0x11 = CALL
            if (!(readByte() == FrpcConstants.MAGIC_NUMBER[0] && readByte() == FrpcConstants.MAGIC_NUMBER[1])) {
                // stream does not start with "CA11", that's a problem
                throw new FrpcDataException("The stream does not start with mandatory \"magic number\" 0xCA 0x11");
            } else {
                // OK, "CALL" is there, check if the protocol version matches
                byte major = readByte();
                byte minor = readByte();
                if(!(major == FrpcConstants.MAGIC_NUMBER[2] && minor == FrpcConstants.MAGIC_NUMBER[3])) {
                    throw new FrpcDataException("Protocol version contained in the \"magic number\" does not match, " +
                            "expected version is " + FrpcConstants.MAGIC_NUMBER[2] + "." + FrpcConstants.MAGIC_NUMBER[3] +
                            ", but version read from the stream is " + major + "." + minor);
                }
            }
        } catch (EndOfStreamException e) {
            throw new FrpcDataException(
                    "Premature end of content, not even a \"magic number\" could be read from the stream", e);
        }
    }

    private String readMethodCall() throws FrpcDataException {
        // read the type identifier
        int data = read();
        // check that is is a method call
        if ((data & FrpcConstants.MASK) == FrpcConstants.TYPE_METHOD_CALL) {
            // read the actual method name
            return readMethodName();
        } else {
            throw new FrpcDataException(
                    "Error while reading method call, number " + data + " is not a \"method call\"" +
                            " identifier");
        }
    }

    private byte readByte() {
        return (byte) read();
    }

    private byte readByte(boolean checkEnd) {
        return (byte) read(checkEnd);
    }

    private int read() throws FrpcDataException {
        return read(true);
    }

    private int read(boolean checkEnd) throws FrpcDataException {
        int data;
        try {
            data = input.read();
        } catch (IOException e) {
            throw new FrpcDataException("Error when reading data from the stream: ", e);
        }
        if (checkEnd && data == -1) {
            throw new EndOfStreamException("End of stream reached while reading data from the input stream");
        }
        return data;
    }

    private FrpcFault readFault() throws FrpcDataException {
        return new FrpcFault((Integer) readObject(), (String) readObject());
    }

    private String readString(int data) throws FrpcDataException {
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
        return new String(buf.array(), StandardCharsets.UTF_8);
    }

    private Number readFloatingPointType() throws FrpcDataException {
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

    private Number readIntegralType(int data, boolean positive) throws FrpcDataException {
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

    private Boolean readBoolean(int data) throws FrpcDataException {
        int octets = data & 1;
        return octets == 1;
    }

    private Object[] readArray(int data) throws FrpcDataException {
        int octets = data & FrpcConstants.MASK_ADD;
        int length = 0;
        for (int i = 0; i <= octets; i++) {
            length |= read() << (i << 3);
        }
        Object[] array = new Object[length];
        for (int i = 0; i < length; i++) {
            array[i] = readObject();
        }
        return array;
    }

    private Map<String, Object> readStruct(int data) throws FrpcDataException {
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
            String key = new String(name.array(), StandardCharsets.UTF_8);
            struct.put(key, readObject());
        }
        return struct;
    }

    private Calendar readDateTime() throws FrpcDataException {
        int data;

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

    private byte[] readBinary(int data) throws FrpcDataException {
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

    private String readMethodName() throws FrpcDataException {
        // read length of method name
        int length = read();

        byte[] arr = new byte[length];
        for (int i = 0; i < length; ++i) {
            arr[i] = (byte) read();
        }
        return new String(arr);
    }

    private Object readObject() throws FrpcDataException {
        Object result;
        // check if there is anything left to read
        int data = read(false);
        // if there is nothing in the stream anymore
        if(data == -1) {
            // return the "no more objects" marker
            return NO_MORE_OBJECTS;
        }
        // check the type of object to be deserialized
        switch (data & FrpcConstants.MASK) {
            case FrpcConstants.TYPE_METHOD_RESPONSE:
                result = readObject();
                break;
            case FrpcConstants.TYPE_FAULT:
                result = readFault();
                break;
            case FrpcConstants.TYPE_STRING:
                result = readString(data);
                break;
            case FrpcConstants.TYPE_DOUBLE:
                result = readFloatingPointType();
                break;
            case FrpcConstants.TYPE_INT_POS:
                result = readIntegralType(data, true);
                break;
            case FrpcConstants.TYPE_INT_NEG:
                result = readIntegralType(data, false);
                break;
            case FrpcConstants.TYPE_BOOL:
                result = readBoolean(data);
                break;
            case FrpcConstants.TYPE_ARRAY:
                result = readArray(data);
                break;
            case FrpcConstants.TYPE_STRUCT:
                result = readStruct(data);
                break;
            case FrpcConstants.TYPE_DATETIME:
                result = readDateTime();
                break;
            case FrpcConstants.TYPE_BINARY:
                result = readBinary(data);
                break;
            case FrpcConstants.TYPE_NULL:
                result = null;
                break;
            default:
                throw new FrpcDataException("Unmarshalling error: unknown type specified by type definition "
                        + (data & FrpcConstants.MASK));
        }
        return result;
    }

}
