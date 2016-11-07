package cz.seznam.frpc.client;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class UnwrappedStructFrpcCallResult extends AbstractUnwrappedFrpcCallResult<Map<String, Object>> implements StructFrpcCallResultOperations<UnwrappedFrpcCallResult, UnwrappedStructFrpcCallResult> {

    UnwrappedStructFrpcCallResult(Map<String, Object> wrapped, int httpResponseStatus, Integer statusCode, String statusMessage) {
        super(wrapped, httpResponseStatus, statusCode, statusMessage);
    }

    @Override
    public boolean isStruct() {
        return true;
    }

    @Override
    public Map<String, Object> asMap() {
        return getMap();
    }

    @Override
    public UnwrappedFrpcCallResult get(String key) {
        checkKey(key);
        // get the name of this resultKey
        Object value = getMap().get(key);
        // and create generic UnwrappedFrpcCallResult out of it
        return new UnwrappedFrpcCallResult(value, httpResponseStatus, statusCode, statusMessage);
    }

    @Override
    public UnwrappedStructFrpcCallResult getStruct(String key) {
        // get resultKey and return it as struct
        return get(key).asStruct();
    }

    protected void checkKey(String key) {
        if(!getMap().containsKey(key)) {
            throw new NoSuchElementException("There is no mapping in the ");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap() {
        return (Map<String, Object>) wrapped;
    }

}
