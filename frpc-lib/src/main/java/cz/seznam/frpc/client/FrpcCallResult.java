package cz.seznam.frpc.client;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcCallResult extends AbstractFrpcCallResult<Object> implements UnwrappableFrpcCallResult, StructConvertibleFrpcCallResult<StructFrpcCallResult>, ArrayConvertibleFrpcCallResult {

    FrpcCallResult(Object wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    @Override
    public UnwrappedFrpcCallResult unwrap() {
        return isStruct() ? asStruct().unwrap() : new UnwrappedFrpcCallResult(wrapped, httpResponseStatus, null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StructFrpcCallResult asStruct() {
        if(!isStruct()) {
            throw new UnsupportedOperationException("Cannot convert this FRPC result to structured form since the object wrapped by this instance is not a map, it is a " + getWrappedType());
        }
        return new StructFrpcCallResult((Map<String, Object>) wrapped, httpResponseStatus);
    }

    @Override
    public <T> ArrayFrpcCallResult<T> asArrayOf(Class<T> type) {
        if(!isArray()) {
            throw new UnsupportedOperationException("Cannot convert this FRPC result to array form since the object wrapped by this instance is not an array, it is a " + getWrappedType());
        }
        return new ArrayFrpcCallResult<>((Object[]) wrapped, httpResponseStatus, type);
    }

}
