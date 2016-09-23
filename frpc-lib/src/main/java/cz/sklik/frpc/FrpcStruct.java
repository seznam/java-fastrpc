package cz.sklik.frpc;

import cz.sklik.frpc.FrpcExceptions.NoElementOfTypeWithName;
import cz.sklik.frpc.FrpcExceptions.NoElementWithName;

import java.nio.ByteBuffer;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;

/**
 * Class representing frpc structure.
 * 
 * Key of structure is String, value can be other frpc data type.
 * 
 * This class provides methods for accessing different data types with given
 * key.
 * 
 * @author Jakub Janda
 * 
 */
public final class FrpcStruct {

    private final HashMap<String, Object> mData;

    /** Creates new instance of FrpcStruct from given HashMap.
     * 
     * Original HashMap isn't copied!
     * 
     * @param source data
     * @return new instance of FrpcStruct
     */
    public static FrpcStruct fromHashMap(HashMap<String, Object> data) {
        return new FrpcStruct(data);
    }

    private FrpcStruct(HashMap<String, Object> data) {
        this.mData = data;
    }

    /** Returns integer at key. 
     * 
     * 
     * @param key
     * @return integer at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    public int getInt(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.INT);
        }

        try {
            return ((Long)value).intValue();
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.INT);
        }

    }

    /** Returns long at key. 
     * 
     * 
     * @param key
     * @return long at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    public long getLong(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.LONG);
        }

        try {
            return (Long)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.LONG);
        }
    }

    /** Returns double at key. 
     * 
     * 
     * @param key
     * @return integer at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    public double getDouble(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.DOUBLE);
        }

        try {
            return (Double)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.DOUBLE);
        }
    }

    /** Returns boolean at key. 
     * 
     * 
     * @param key
     * @return boolean at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    public boolean getBoolean(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.BOOLEAN);
        }

        try {
            return (Boolean)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.BOOLEAN);
        }
    }

    /** Returns string at key. 
     * 
     * 
     * @param key
     * @return string at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    public String getString(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.STRING);
        }

        try {
            return (String)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.STRING);
        }
    }

    /** Returns FrpcStruct at key. 
     * 
     * 
     * @param key
     * @return FrpcStruct at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    @SuppressWarnings("unchecked")
    public FrpcStruct getFrpcStruct(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.STRUCT);
        }

        try {
            return FrpcStruct.fromHashMap((HashMap<String, Object>)value);
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.STRUCT);
        }
    }

    /** Returns FrpcArray at key. 
     * 
     * 
     * @param key
     * @return FrpcArray at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    public FrpcArray getFrpcArray(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.ARRAY);
        }

        try {
            return FrpcArray.fromArray((Object[])value);
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.ARRAY);
        }
    }

    /** Returns datetime as GregorianCalendar at key. 
     * 
     * 
     * @param key
     * @return datetime as GregorianCalendar at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    public GregorianCalendar getDateTime(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.DATETIME);
        }

        try {
            return (GregorianCalendar)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.DATETIME);
        }
    }

    /** Returns binary blob as ByteBuffer at key. 
     * 
     * 
     * @param key
     * @return binary blob as ByteBuffer at key
     * 
     * @throws FrpcExceptions.ElementWithKeyNullException
     * @throws NoElementOfTypeWithName
     */
    public ByteBuffer getByteBuffer(String key) {
        Object value = getObject(key);

        if (value == null) {
            throw new FrpcExceptions.ElementWithKeyNullException(key, FrpcExceptions.BINARY);
        }

        try {
            return (ByteBuffer)value;
        } catch (ClassCastException e) {
            throw new NoElementOfTypeWithName(key, value, FrpcExceptions.BINARY);
        }
    }
    
    private Object getObject(String key) {
        if (!this.mData.containsKey(key)) {
            throw new NoElementWithName(key, this);
        } else {
            return this.mData.get(key);
        }
    }

    /** Returns keys of this FrpcStruct.
     * 
     * @return FrpcStruct keys
     */
    public Set<String> getKeys() {
        return this.mData.keySet();
    }

    /** Returns source data of this FrpcStruct.
     * 
     * @return source data
     */
    public HashMap<String, Object> getSourceData() {
        return this.mData;
    }

    /** Puts new value with key into FrpcStruct.
     * 
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        this.mData.put(key, value);
    }

    public String toString() {
        return FrpcLog.frpcToString(this.mData);
    }

    /** Check, if struct contains value with key.
     * 
     * @param key
     * @return true, if struct contains key, otherwise false
     */
    public boolean containsKey(String key) {
        return this.mData.containsKey(key);
    }
}
