package cz.sklik.frpc;

import cz.sklik.frpc.FrpcExceptions.NoElementOfTypeOnIndex;

import java.nio.ByteBuffer;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Class representing frpc array type.
 * 
 * Frpc array can hold other fprc data types (primitive data types, frpc array,
 * frpc struct, datetime).
 * 
 * This class provides methods for accessing different data types in array at
 * given index.
 * 
 * @author Jakub Janda
 * 
 */
public final class FrpcArray {

    private final Object[] mData;

    /**
     * Creates new instance of FrpcArray with given array as data source.
     * 
     * Original array isn't copied!
     * 
     * @param data object
     * 
     * @return new instance of FrpcArray with original array as data source
     */
    public static FrpcArray fromArray(Object[] data) {
        return new FrpcArray(data);
    }

    private FrpcArray(Object[] data) {
        this.mData = data;
    }

    /**
     * Returns integer at given index.
     * 
     * @param index
     * 
     * @return integer at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    public int getInt(int index) {
        Object value = this.mData[index];

        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.INT);
        }

        try {
            return ((Long)value).intValue();
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.INT);
        }
    }

    /**
     * Returns long at given index.
     * 
     * @param index
     * 
     * @return long at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    public long getLong(int index) {
        Object value = this.mData[index];
        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.LONG);
        }
        try {
            return (Long)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.LONG);
        }
    }

    /**
     * Returns double at given index.
     * 
     * @param index
     * 
     * @return double at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    public double getDouble(int index) {
        Object value = this.mData[index];

        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.DOUBLE);
        }

        try {
            return (Double)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.DOUBLE);
        }
    }

    /**
     * Returns boolean at given index.
     * 
     * @param index
     * 
     * @return boolean at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    public boolean getBoolean(int index) {
        Object value = this.mData[index];

        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.BOOLEAN);
        }

        try {
            return (Boolean)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.BOOLEAN);
        }
    }

    /**
     * Returns String at given index.
     * 
     * @param index
     * 
     * @return String at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    public String getString(int index) {
        Object value = this.mData[index];

        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.STRING);
        }

        try {
            return (String)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.STRING);
        }
    }

    /**
     * Returns FrpcStruct at given index.
     * 
     * @param index
     * 
     * @return FrpcStruct at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    @SuppressWarnings("unchecked")
    public FrpcStruct getFrpcStruct(int index) {
        Object value = this.mData[index];

        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.STRUCT);
        }

        try {
            return FrpcStruct.fromHashMap((HashMap<String, Object>)value);
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.STRUCT);
        }
    }

    /**
     * Returns FrpcArray at given index.
     * 
     * @param index
     * 
     * @return FrpcArray at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    public FrpcArray getFrpcArray(int index) {
        Object value = mData[index];

        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.ARRAY);
        }

        try {
            return FrpcArray.fromArray((Object[])value);
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.ARRAY);
        }
    }

    /**
     * Returns frpc datetime as GregorianCalendar at given index.
     * 
     * @param index
     * 
     * @return datetime as GregorianCalendar at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    public GregorianCalendar getDateTime(int index) {
        Object value = this.mData[index];

        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.DATETIME);
        }

        try {
            return (GregorianCalendar)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.DATETIME);
        }
    }

    /**
     * Returns binary blob as ByteBuffer as GregorianCalendar at given index.
     * 
     * @param index
     * 
     * @return binary blob as ByteBuffer as GregorianCalendar at index
     * 
     * @throws FrpcExceptions.ElementAtIndexNullException
     * @throws NoElementOfTypeOnIndex
     */
    public ByteBuffer getByteBuffer(int index) {
        Object value = this.mData[index];

        if (value == null) {
            throw new FrpcExceptions.ElementAtIndexNullException(index, FrpcExceptions.BINARY);
        }

        try {
            return (ByteBuffer)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeOnIndex(index, value, FrpcExceptions.BINARY);
        }
    }

    /**
     * Returns source array.
     * 
     * @return source array of objects
     */
    public Object[] getSourceData() {
        return this.mData;
    }

    /**
     * Returns length of array.
     * 
     * @return length of array
     */
    public int getLength() {
        return this.mData.length;
    }

    public String toString() {
        return FrpcLog.frpcToString(this.mData);
    }
}
