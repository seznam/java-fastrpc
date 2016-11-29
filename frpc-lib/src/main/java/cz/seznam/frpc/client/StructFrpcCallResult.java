package cz.seznam.frpc.client;

import cz.seznam.frpc.core.FrpcType;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class StructFrpcCallResult extends AbstractFrpcCallResult<Map<String, Object>> {

    StructFrpcCallResult(Map<String, Object> wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    @Override
    public boolean isStruct() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isFault() {
        return false;
    }

    public Map<String, Object> asMap() {
        return wrapped;
    }

    public FrpcCallResult get(String key) {
        checkKey(key);
        // get the value of this resultKey
        Object value = wrapped.get(key);
        // and create generic UnwrappedFrpcCallResult out of it
        return new FrpcCallResult(value, httpResponseStatus);
    }

    public StructFrpcCallResult getStruct(String key) {
        return get(key).asStruct();
    }

    public ArrayFrpcCallResult<Object> getArray(String key) {
        return getArray(key, Object.class);
    }

    public <A> ArrayFrpcCallResult<A> getArray(String key, Class<A> arrayType) {
        return get(key).asArrayOf(arrayType);
    }

    public <A> ArrayFrpcCallResult<A> getArray(String key, FrpcType<A> arrayType) {
        return get(key).asArrayOf(arrayType);
    }

    protected void checkKey(String key) {
        if(!wrapped.containsKey(key)) {
            throw new NoSuchElementException("There is no mapping in the map for key \"" + key + "\"");
        }
    }

}
