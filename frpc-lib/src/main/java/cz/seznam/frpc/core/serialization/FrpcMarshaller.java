package cz.seznam.frpc.core.serialization;

import cz.seznam.frpc.core.FrpcConstants;
import cz.seznam.frpc.core.FrpcDataException;
import cz.seznam.frpc.core.transport.FrpcFault;
import cz.seznam.frpc.core.transport.FrpcRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Core component of the framework. {@code FrpcMarshaller} transforms plain Java objects into binary data and writes
 * them into a stream.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcMarshaller {

    /**
     * The stream to write serialized objects into.
     */
    protected OutputStream outputStream;

    /**
     * Creates new marshaller writing data into given stream.
     *
     * @param outputStream stream to write serialized objects into
     */
    public FrpcMarshaller(OutputStream outputStream) {
        this.outputStream = Objects.requireNonNull(outputStream);
    }

    /**
     * Writes {@link FrpcRequest} into the stream. As per protocol specification, this process works as follows:
     * <ol>
     *     <li>
     *         Magic number is written.
     *     </li>
     *     <li>
     *         Method name (a string value) is written.
     *     </li>
     *     <li>
     *         Request parameters are written one by one in order of iteration of the list holding them.
     *     </li>
     * </ol>
     *
     * @param request {@code FRPC} request to be written into the stream
     *
     * @throws FrpcDataException if anything goes wrong during serialization
     */
    public void writeRequest(FrpcRequest request) throws FrpcDataException {
        Objects.requireNonNull(request, "Request must not be null");
        try {
            // initialize non-data type
            writeMagic();
            // write method call identifier
            writeMethodCallIdentifier(request.getMethodName());
            // write method parameters
            for(Object param : request.getParameters()) {
                writeObject(param);
            }
        } catch (IOException e) {
            throw new FrpcDataException("Error while writing FRPC request into the stream", e);
        }
    }

    /**
     * Writes an object representing the  into the stream. As per protocol specification, this process works as follows:
     * <ol>
     *     <li>
     *         Magic number is written.
     *     </li>
     *     <li>
     *         Method response type is written. If the response object is a {@link FrpcFault}, then response type
     *         indicating a fault is written. Otherwise a response type indicating single-object {@code FRPC} response
     *         is written.
     *     </li>
     *     <li>
     *         Actual data is written. In case of {@code FrpcFault}, this means status message and status code (in this
     *         order), in other cases it means the response object.
     *     </li>
     * </ol>
     *
     * @param response response object to be written into the stream
     * @throws FrpcDataException if anything goes wrong during serialization
     */
    public void writeResponse(Object response) throws FrpcDataException {
        // if the object is a fault, write fault
        if(response instanceof FrpcFault) {
            writeFault(((FrpcFault) response));
            return;
        }
        // write any other response
        try {
            // initialize non-data type
            writeMagic();
            // write method response identifier
            writeMethodResponseIdentifier();
            // write the object
            writeObject(response);
        } catch (IOException e) {
            throw new FrpcDataException("Error while writing FRPC response into the stream", e);
        }
    }

    private void writeFault(FrpcFault fault) throws FrpcDataException {
        Objects.requireNonNull(fault, "Fault must not be null");
        try {
            // initialize non-data type
            writeMagic();
            // write fault identifier
            writeFaultIdentifier();
            // write status code and status message
            writeObject(fault.getStatusCode());
            writeObject(fault.getStatusMessage());
        } catch (IOException e) {
            throw new FrpcDataException("Error while writing FRPC fault into the stream", e);
        }
    }

    private void writeMagic() throws IOException {
        outputStream.write(FrpcConstants.MAGIC_NUMBER);
    }

    private void writeMethodCallIdentifier(String methodName) throws IOException {
        outputStream.write(FrpcConstants.TYPE_METHOD_CALL);
        outputStream.write(methodName.length());
        outputStream.write(methodName.getBytes(StandardCharsets.UTF_8));
    }

    private void writeMethodResponseIdentifier() throws IOException {
        outputStream.write(FrpcConstants.TYPE_METHOD_RESPONSE);
    }

    private void writeFaultIdentifier() throws IOException {
        outputStream.write(FrpcConstants.TYPE_FAULT);
    }

    private void writeArray(int numOfItems) throws IOException {
        int octets = 0;
        while ((numOfItems >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        outputStream.write(FrpcConstants.TYPE_ARRAY | octets);

        for (int i = 0; i <= octets; i++) {
            outputStream.write((numOfItems >> (i << 3)) & 0xff);
        }
    }

    private void writeBinary(byte[] data) throws IOException {
        int size = data.length;
        int octets = 0;
        while ((size >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        outputStream.write(FrpcConstants.TYPE_BINARY | octets);

        for (int i = 0; i <= octets; i++) {
            outputStream.write((size >> (i << 3)) & 0xff);
        }
        outputStream.write(data);
    }

    private void writeBool(boolean value) throws IOException {
        outputStream.write(FrpcConstants.TYPE_BOOL | (value ? 1 : 0));
    }

    private void writeDateTime(int timeStamp, int weekDay, int year, int month, int day, int hour, int min,
                               int sec, int timeZone) throws IOException {

        int s1 = (sec & 0x1f) << 3 | (weekDay & 0x07);
        int s2 = ((min & 0x3f) << 1) | ((sec & 0x20) >> 5) | ((hour & 0x01) << 7);
        int s3 = ((hour & 0x1e) >> 1) | ((day & 0x0f) << 4);
        int s4 = ((day & 0x1f) >> 4) | ((month & 0x0f) << 1) | ((year & 0x07) << 5);
        int s5 = ((year & 0x07f8) >> 3);

        outputStream.write(FrpcConstants.TYPE_DATETIME);
        outputStream.write(timeZone);

        for (int i = 0; i <= 3; i++) {
            outputStream.write(timeStamp >> (i << 3) & 0xff);
        }

        outputStream.write(s1 & 0xff);
        outputStream.write(s2 & 0xff);
        outputStream.write(s3 & 0xff);
        outputStream.write(s4 & 0xff);
        outputStream.write(s5 & 0xff);

    }

    private void writeDouble(double value) throws IOException {
        long binaryValue = Double.doubleToLongBits(value);
        outputStream.write(FrpcConstants.TYPE_DOUBLE);

        for (int i = 0; i < 8; i++) {
            outputStream.write((int) ((binaryValue >> (i << 3)) & 0xff));
        }
    }

    private void writeInt(int value) throws IOException {
        int octets = 0;
        byte typeOfInt = FrpcConstants.TYPE_INT_POS;
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                throw new IOException(
                        "Error occurred while writing int value, Integer.MIN_VALUE cannot be serialized as int");
            }
            typeOfInt = FrpcConstants.TYPE_INT_NEG;
            value = -value;
        }
        while ((value >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }

        outputStream.write(typeOfInt | octets);

        for (int i = 0; i <= octets; i++) {
            outputStream.write(value >> (i << 3) & 0xff);
        }
    }

    private void writeInt(long value) throws IOException {
        int octets = 0;
        byte intType = FrpcConstants.TYPE_INT_POS;
        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                throw new IOException(
                        "Error occurred while writing long value, Long.MIN_VALUE cannot be serialized");
            }
            intType = FrpcConstants.TYPE_INT_NEG;
            value = -value;
        }
        long shiftedVal;
        while ((value >> (octets << 3)) > 255 && octets < 7) {
            shiftedVal = (value >> (octets << 3));
            if (shiftedVal < 255) {
                break;
            }
            octets++;
        }

        outputStream.write(intType | octets);

        for (int i = 0; i <= octets; i++) {
            outputStream.write((int) (value >> (i << 3) & 0xff));
        }
    }

    private void writeString(String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        int stringLength = bytes.length;
        int octets = 0;
        while ((stringLength >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        outputStream.write(FrpcConstants.TYPE_STRING | octets);

        for (int i = 0; i <= octets; i++) {
            outputStream.write((stringLength >> (i << 3)) & 0xff);
        }
        outputStream.write(bytes);
    }

    private void writeStruct(int numOfItems) throws IOException {
        int octets = 0;
        while ((numOfItems >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        outputStream.write(FrpcConstants.TYPE_STRUCT | octets);

        for (int i = 0; i <= octets; i++) {
            outputStream.write((numOfItems >> (i << 3)) & 0xff);
        }
    }

    private void writeStructMember(String memberName) throws IOException {
        outputStream.write(memberName.length());
        outputStream.write(memberName.getBytes());
    }

    private void writeNull() throws IOException {
        outputStream.write(FrpcConstants.TYPE_NULL);
    }

    private void writeCalendar(Calendar calendar) throws IOException {
        writeDateTime((new Long(calendar.getTimeInMillis() / 1000)).intValue(),
                calendar.get(Calendar.DAY_OF_WEEK) - 1, calendar.get(Calendar.YEAR)
                        - FrpcConstants.DATE_YEAR_OFFSET,
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND), calendar.get(Calendar.ZONE_OFFSET)
                        / 1000 / 60 / 15 + calendar.get(Calendar.DST_OFFSET) / 1000 / 60
                        / 15);
    }
    
    private void writeObject(Object object) throws IOException {
        if (object == null) {
            writeNull();
        } else if (object instanceof Object[]) {
            Object[] array = (Object[]) object;
            writeArray(array.length);
            for (Object obj : array) {
                writeObject(obj);
            }
        } else if(object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            int size = collection.size();
            writeArray(size);
            for (Object aList : collection) {
                writeObject(aList);
            }
        } else if (object instanceof double[]) {
            double[] array = (double[]) object;
            writeArray(array.length);
            for (double d : array) {
                writeObject(d);
            }
        } else if (object instanceof float[]) {
            float[] array = (float[]) object;
            writeArray(array.length);
            for (float d : array) {
                writeObject(d);
            }
        } else if (object instanceof int[]) {
            int[] array = (int[]) object;
            writeArray(array.length);
            for (int d : array) {
                writeObject(d);
            }
        } else if (object instanceof long[]) {
            long[] array = (long[]) object;
            writeArray(array.length);
            for (long d : array) {
                writeObject(d);
            }
        } else if (object instanceof byte[]) {
            writeBinary((byte[]) object);
        } else if (object instanceof Float) {
            writeDouble(((Float) object).doubleValue());
        } else if (object instanceof Double) {
            writeDouble((Double) object);
        } else if (object instanceof Integer) {
            writeInt((Integer) object);
        } else if (object instanceof Long) {
            writeInt((Long) object);
        } else if (object instanceof String) {
            writeString((String) object);
        } else if (object instanceof Boolean) {
            writeBool((Boolean) object);
        } else if (object instanceof Map<?, ?>) {
            Map<?, ?> struct = (Map<?, ?>) object;
            writeStruct(struct.size());
            for (Map.Entry<?, ?> entry : struct.entrySet()) {
                // check that the key is a string
                Object key = entry.getKey();
                if(key != null && key.getClass() != String.class) {
                    throw new FrpcDataException(
                            "Cannot serialize value " + key + " as map key, only String is valid type for map keys");
                }
                writeStructMember((String) entry.getKey());
                writeObject(entry.getValue());
            }
        } else if (object instanceof Calendar) {
            writeCalendar((Calendar) object);
        } else if (object instanceof Date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date) object);
            writeCalendar(calendar);
        } else if (object instanceof LocalDateTime) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(((LocalDateTime) object).atZone(ZoneId.systemDefault()).toInstant()));
            writeCalendar(calendar);
        } else if (object instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) object;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(zonedDateTime.toInstant()));
            calendar.setTimeZone(TimeZone.getTimeZone(zonedDateTime.getZone()));
            writeCalendar(calendar);
        } else {
            throw new FrpcDataException("Error while marshalling object " + object +
                    ", type " + object.getClass() + " is not a supported FRPC type");
        }
    }
}
