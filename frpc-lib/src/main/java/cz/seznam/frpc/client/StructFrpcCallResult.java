package cz.seznam.frpc.client;

import cz.seznam.frpc.core.FrpcType;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Class representing a {@code FRPC} call result converted into a "structure" which is a {@code Map<String, Object>}
 * in Java. Provides convenience methods for accessing individual "members" of the structure and potentially converting
 * these into other types.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class StructFrpcCallResult extends AbstractFrpcCallResult<Map<String, Object>> {

    StructFrpcCallResult(Map<String, Object> wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    /**
     * Returns the underlying map as-is, no copy is made.
     *
     * @return the underlying map, as-is
     */
    public Map<String, Object> asMap() {
        return wrapped;
    }

    /**
     * Returns a value mapped under given key wrapped in {@code FrpcCallResult} so that it can be further manipulated
     * with.
     *
     * @param key key to return wrapped value for
     * @return a value mapped under given key wrapped in {@code FrpcCallResult}
     * @throws NoSuchElementException if these is no mapping for given key
     */
    public FrpcCallResult<Object> get(String key) {
        checkKey(key);
        // get the value of this resultKey
        Object value = wrapped.get(key);
        // and create generic UnwrappedFrpcCallResult out of it
        return new FrpcCallResult<>(value, httpResponseStatus);
    }

    /**
     * Convenience method for calling {@link #get(String)} with given parameter and subsequently calling
     * {@link FrpcCallResult#asStruct()} on the result.
     *
     * @param key key to return wrapped value for
     * @return result of operations described above
     */
    public StructFrpcCallResult getStruct(String key) {
        return get(key).asStruct();
    }

    /**
     * Convenience method for calling {@link #get(String)} with given parameter and subsequently calling
     * {@link FrpcCallResult#asObjectArray()} on the result.
     *
     * @param key key to return wrapped value for
     * @return result of operations described above
     */
    public ArrayFrpcCallResult<Object> getObjectArray(String key) {
        return get(key).asObjectArray();
    }

    /**
     * Convenience method for calling {@link #get(String)} with first parameter and subsequently calling
     * {@link FrpcCallResult#asArrayOf(Class)} on the result with second parameter.
     *
     * @param key key to return wrapped value for
     * @return result of operations described above
     */
    public <A> ArrayFrpcCallResult<A> getArrayOf(String key, Class<A> arrayType) {
        return get(key).asArrayOf(arrayType);
    }

    /**
     * Convenience method for calling {@link #get(String)} with first parameter and subsequently calling
     * {@link FrpcCallResult#asArrayOf(FrpcType)} on the result with second parameter.
     *
     * @param key key to return wrapped value for
     * @return result of operations described above
     */
    public <A> ArrayFrpcCallResult<A> getArrayOf(String key, FrpcType<A> arrayType) {
        return get(key).asArrayOf(arrayType);
    }

    private void checkKey(String key) {
        if(!wrapped.containsKey(key)) {
            throw new NoSuchElementException("There is no mapping in the map for key \"" + key + "\"");
        }
    }

}
