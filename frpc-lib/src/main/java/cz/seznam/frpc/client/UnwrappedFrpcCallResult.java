package cz.seznam.frpc.client;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class UnwrappedFrpcCallResult extends AbstractUnwrappedFrpcCallResult<Object> implements StructConvertibleFrpcCallResult<UnwrappedStructFrpcCallResult>, ArrayConvertibleFrpcCallResult {

    UnwrappedFrpcCallResult(Object wrapped, int httpResponseStatus, Integer statusCode, String statusMessage) {
        super(wrapped, httpResponseStatus, statusCode, statusMessage);
    }

    @Override
    @SuppressWarnings("unchecked")
    public UnwrappedStructFrpcCallResult asStruct() {
        if(!isStruct()) {
            throw new UnsupportedOperationException("Cannot convert this FRPC result to structured form since the object wrapped by this instance is not a map, it is a " + getWrappedType());
        }
        return new UnwrappedStructFrpcCallResult((Map<String, Object>) wrapped, httpResponseStatus, statusCode, statusMessage);
    }

    @Override
    public <T> ArrayFrpcCallResult<T> asArrayOf(Class<T> type) {
        if(!isArray()) {
            throw new UnsupportedOperationException("Cannot convert this FRPC result to array form since the object wrapped by this instance is not an array, it is a " + getWrappedType());
        }
        return new ArrayFrpcCallResult<>((Object[]) wrapped, httpResponseStatus, type);
    }

}
