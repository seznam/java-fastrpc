package cz.seznam.frpc.client;

import cz.seznam.frpc.FrpcUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class StructFrpcCallResult extends AbstractFrpcCallResult<Map<String, Object>> implements UnwrappableFrpcCallResult, StructFrpcCallResultOperations<AbstractFrpcCallResult, StructFrpcCallResult> {

    StructFrpcCallResult(Map<String, Object> wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    @Override
    public boolean isStruct() {
        return true;
    }

    @Override
    public UnwrappedFrpcCallResult unwrap() {
        // create copy of wrapped map
        Map<String, Object> copy = new HashMap<>(wrapped);
        // remove mapping for status code and status message
        Integer statusCode = (Integer) copy.remove(FrpcUtils.STATUS_KEY);
        String statusMessage = (String) copy.remove(FrpcUtils.STATUS_MESSAGE_KEY);
        // check how many mappings are left in the map
        Object toWrap;
        if(copy.size() == 1) {
            // if there is just one mapping left, pull the value out of the map
            toWrap = copy.values().iterator().next();
        } else {
            // otherwise wrap the resulting map
            toWrap = copy;
        }
        // and return new UnwrappedFrpcCallResult wrapping that object
        return new UnwrappedFrpcCallResult(toWrap, httpResponseStatus, statusCode, statusMessage);
    }

    @Override
    public Map<String, Object> asMap() {
        return wrapped;
    }

    @Override
    public FrpcCallResult get(String key) {
        checkKey(key);
        // get the value of this key
        Object value = wrapped.get(key);
        // and create generic UnwrappedFrpcCallResult out of it
        return new FrpcCallResult(value, httpResponseStatus);
    }

    @Override
    public StructFrpcCallResult getStruct(String key) {
        return get(key).asStruct();
    }

    protected void checkKey(String key) {
        if(!wrapped.containsKey(key)) {
            throw new NoSuchElementException("There is no mapping in the ");
        }
    }

}
