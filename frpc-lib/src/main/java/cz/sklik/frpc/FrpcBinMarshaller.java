package cz.sklik.frpc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;

public class FrpcBinMarshaller {

    final String LOGTAG = "FRPC";

    private OutputStream mWriter;

    public FrpcBinMarshaller(OutputStream w) {
        mWriter = w;
    }

    public void packMagic() throws FrpcDataException {
        try {
            mWriter.write(FrpcInternals.MAGIC_NUMBER);
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packMethodCall(String methodName) throws FrpcDataException {
        try {
            mWriter.write(FrpcInternals.TYPE_METHOD_CALL);
            mWriter.write(methodName.length());
            mWriter.write(methodName.getBytes());
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packMethodResponse() throws FrpcDataException {
        try {
            mWriter.write(FrpcInternals.TYPE_METHOD_RESPONSE);
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packArray(int numOfItems) throws FrpcDataException {
        int octets = 0;
        while ((numOfItems >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        try {
            mWriter.write(FrpcInternals.TYPE_ARRAY | octets);

            for (int i = 0; i <= octets; i++) {
                mWriter.write((numOfItems >> (i << 3)) & 0xff);
            }
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packBinary(byte[] value) throws FrpcDataException {
        int size = value.length;
        int octets = 0;
        while ((size >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        try {
            mWriter.write(FrpcInternals.TYPE_BINARY | octets);

            for (int i = 0; i <= octets; i++) {
                mWriter.write((size >> (i << 3)) & 0xff);
            }
            mWriter.write(value);
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packBinary(ByteBuffer value) throws FrpcDataException {
        byte[] data = value.array();
        int size = data.length;
        int octets = 0;
        while ((size >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        try {
            mWriter.write(FrpcInternals.TYPE_BINARY | octets);

            for (int i = 0; i <= octets; i++) {
                mWriter.write((size >> (i << 3)) & 0xff);
            }
            mWriter.write(data);
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packBool(boolean value) throws FrpcDataException {
        try {
            mWriter.write(FrpcInternals.TYPE_BOOL | (value ? 1 : 0));
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packDateTime(int timeStamp, int weekDay, int year, int month, int day, int hour, int min,
                             int sec, int timeZone) throws FrpcDataException {

        int s1 = (sec & 0x1f) << 3 | (weekDay & 0x07);
        int s2 = ((min & 0x3f) << 1) | ((sec & 0x20) >> 5) | ((hour & 0x01) << 7);
        int s3 = ((hour & 0x1e) >> 1) | ((day & 0x0f) << 4);
        int s4 = ((day & 0x1f) >> 4) | ((month & 0x0f) << 1) | ((year & 0x07) << 5);
        int s5 = ((year & 0x07f8) >> 3);
        try {
            mWriter.write(FrpcInternals.TYPE_DATETIME);
            mWriter.write(timeZone);

            for (int i = 0; i <= 3; i++) {
                mWriter.write(timeStamp >> (i << 3) & 0xff);
            }

            mWriter.write(s1 & 0xff);
            mWriter.write(s2 & 0xff);
            mWriter.write(s3 & 0xff);
            mWriter.write(s4 & 0xff);
            mWriter.write(s5 & 0xff);
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }

    }

    public void packDouble(double value) throws FrpcDataException {
        long binaryValue = Double.doubleToLongBits(value);
        try {
            mWriter.write(FrpcInternals.TYPE_DOUBLE);

            for (int i = 0; i < 8; i++) {
                mWriter.write((int) ((binaryValue >> (i << 3)) & 0xff));
            }
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packInt(int value) throws FrpcDataException {
        int octets = 0;
        byte typeOfInt = FrpcInternals.TYPE_INT_POS;
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                throw new FrpcDataException(
                        "Exception in frpc packInt: Value is smaller then minimal value");
            }
            typeOfInt = FrpcInternals.TYPE_INT_NEG;
            value = -value;
        }
        while ((value >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        try {
            mWriter.write(typeOfInt | octets);

            for (int i = 0; i <= octets; i++) {
                mWriter.write(value >> (i << 3) & 0xff);
            }
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packInt(long value) throws FrpcDataException {
        int octets = 0;
        byte typeOfInt = FrpcInternals.TYPE_INT_POS;
        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                throw new FrpcDataException(
                        "Exception in frpc packInt: Value is smaller then minimal value");
            }
            typeOfInt = FrpcInternals.TYPE_INT_NEG;
            value = -value;
        }
        long shiftedVal = value;
        while ((value >> (octets << 3)) > 255 && octets < 7) {
            shiftedVal = (value >> (octets << 3));
            if (shiftedVal < 255) {
                break;
            }
            octets++;
        }
        try {
            mWriter.write(typeOfInt | octets);

            for (int i = 0; i <= octets; i++) {
                mWriter.write((int) (value >> (i << 3) & 0xff));
            }
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packString(String string) throws FrpcDataException {
        byte[] decodedString;
        try {
            decodedString = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw new FrpcDataException("Chyba pri decodovani dat...");
        }
        int stringLength = decodedString.length;
        int octets = 0;
        while ((stringLength >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        try {
            mWriter.write(FrpcInternals.TYPE_STRING | octets);

            for (int i = 0; i <= octets; i++) {
                mWriter.write((stringLength >> (i << 3)) & 0xff);
            }
            mWriter.write(decodedString);
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packStruct(int numOfItems) throws FrpcDataException {
        int octets = 0;
        while ((numOfItems >> (octets << 3)) > 255 && octets < 7) {
            octets++;
        }
        try {
            mWriter.write(FrpcInternals.TYPE_STRUCT | octets);

            for (int i = 0; i <= octets; i++) {
                mWriter.write((numOfItems >> (i << 3)) & 0xff);
            }
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packStructMember(String memeberName) throws FrpcDataException {
        try {
            mWriter.write(memeberName.length());

            mWriter.write(memeberName.getBytes());
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packNull() throws FrpcDataException {
        try {
            mWriter.write(FrpcInternals.TYPE_NULL);
        } catch (IOException e) {
            throw new FrpcDataException("IO exception when sending frpc request: " + e);
        }
    }

    public void packItem(Object item) throws FrpcDataException {
        if (item == null) {
            packNull();
        } else if (item instanceof Object[]) {
            Object[] array = (Object[]) item;
            packArray(array.length);
            for (Object obj : array) {
                packItem(obj);
            }
        } else if (item instanceof List) {
            List<?> list = (List<?>) item;
            int size = list.size();
            packArray(size);
            for (int i = 0; i < size; i++) {
                packItem(list.get(i));
            }
        } else if (item instanceof double[]) {
            double[] array = (double[]) item;
            packArray(array.length);
            for (double d : array) {
                packItem(d);
            }
        } else if (item instanceof float[]) {
            float[] array = (float[]) item;
            packArray(array.length);
            for (float d : array) {
                packItem(d);
            }
        } else if (item instanceof int[]) {
            int[] array = (int[]) item;
            packArray(array.length);
            for (int d : array) {
                packItem(d);
            }
        } else if (item instanceof long[]) {
            long[] array = (long[]) item;
            packArray(array.length);
            for (long d : array) {
                packItem(d);
            }
        } else if (item instanceof byte[]) {
            packBinary((byte[]) item);
        } else if (item instanceof Float) {
            packDouble(((Float) item).doubleValue());
        } else if (item instanceof Double) {
            packDouble((Double) item);
        } else if (item instanceof Integer) {
            packInt((int) ((Integer) item));
        } else if (item instanceof Long) {
            packInt((long) ((Long) item));
        } else if (item instanceof String) {
            packString((String) item);
        } else if (item instanceof ByteBuffer) {
            packBinary((ByteBuffer) item);
        } else if (item instanceof Boolean) {
            packBool((Boolean) item);
        } else if (item instanceof HashMap<?, ?>) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> struct = (HashMap<String, Object>) item;
            packStruct(struct.size());
            Set<Map.Entry<String, Object>> items = struct.entrySet();
            Iterator<Map.Entry<String, Object>> iter = items.iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Object> entry = iter.next();
                packStructMember(entry.getKey());
                packItem(entry.getValue());
            }
        } else if (item instanceof GregorianCalendar) {
            GregorianCalendar date = (GregorianCalendar) item;
            packDateTime((new Long(date.getTimeInMillis() / 1000)).intValue(),
                    date.get(GregorianCalendar.DAY_OF_WEEK) - 1, date.get(GregorianCalendar.YEAR)
                            - FrpcInternals.DATE_YEAR_OFFSET,
                    date.get(GregorianCalendar.MONTH) + 1, date.get(GregorianCalendar.DATE),
                    date.get(GregorianCalendar.HOUR_OF_DAY), date.get(GregorianCalendar.MINUTE),
                    date.get(GregorianCalendar.SECOND), date.get(GregorianCalendar.ZONE_OFFSET)
                            / 1000 / 60 / 15 + date.get(GregorianCalendar.DST_OFFSET) / 1000 / 60
                            / 15);
        } else {
            throw new FrpcDataException(
                    "Errorn in frpc bin marschaller: Uknown type to marschall!!! - "
                            + item.toString());
        }
    }
}
