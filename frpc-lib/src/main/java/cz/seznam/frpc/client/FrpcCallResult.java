package cz.seznam.frpc.client;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcCallResult extends AbstractFrpcCallResult<Object> {

    FrpcCallResult(Object wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    @SuppressWarnings("unchecked")
    public StructFrpcCallResult asStruct() {
        if(isFault() || !isStruct()) {
            throw new IllegalStateException("Cannot convert this FRPC result to structured form since the object wrapped by this instance is not a map, it is a " + getWrappedType());
        }
        return new StructFrpcCallResult((Map<String, Object>) wrapped, httpResponseStatus);
    }

    public ArrayFrpcCallResult<Object> asArray() {
        return asArrayOf(Object.class);
    }

    public <T> ArrayFrpcCallResult<T> asArrayOf(Class<T> type) {
        if(isFault() || !isArray()) {
            throw new IllegalStateException("Cannot convert this FRPC result to array form since the object wrapped by this instance is not an array, it is a " + getWrappedType());
        }
        return new ArrayFrpcCallResult<>((Object[]) wrapped, httpResponseStatus, type);
    }

}
