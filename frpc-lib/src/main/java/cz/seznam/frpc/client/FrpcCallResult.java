package cz.seznam.frpc.client;

import cz.seznam.frpc.core.ConversionResult;
import cz.seznam.frpc.core.FrpcType;
import cz.seznam.frpc.core.FrpcTypes;
import cz.seznam.frpc.core.FrpcTypesConverter;
import cz.seznam.frpc.core.transport.FrpcFault;

import java.lang.reflect.Type;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcCallResult extends AbstractFrpcCallResult<Object> {

    FrpcCallResult(Object wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    public FrpcFault asFault() {
        if(!isFault()) {
            throw new IllegalStateException("Cannot convert this FRPC result to FRPC fault form since the object wrapped by this instance is not a fault, it is a " + getWrappedType());
        }
        return as(FrpcFault.class);
    }

    @SuppressWarnings("unchecked")
    public StructFrpcCallResult asStruct() {
        if(isFault() || !isStruct()) {
            throw new IllegalStateException("Cannot convert this FRPC result to structured form since the object wrapped by this instance is not a map, it is a " + getWrappedType());
        }
        return new StructFrpcCallResult(as(FrpcTypes.STRUCT), httpResponseStatus);
    }

    public ArrayFrpcCallResult<Object> asArray() {
        return asArrayOf(Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayFrpcCallResult<T> asArrayOf(Class<T> type) {
        return (ArrayFrpcCallResult<T>) asArrayOf((Type) type);
    }

    @SuppressWarnings("unchecked")
    public <T> ArrayFrpcCallResult<T> asArrayOf(FrpcType<T> type) {
        return (ArrayFrpcCallResult<T>) asArrayOf(type.getGenericType());
    }

    @SuppressWarnings("unchecked")
    private <T> ArrayFrpcCallResult<T> asArrayOf(Type type) {
        if(isFault() || !isArray()) {
            throw new IllegalStateException("Cannot convert this FRPC result to array form since the object wrapped by this instance is not an array, it is a " + getWrappedType());
        }
        ConversionResult conversionResult = FrpcTypesConverter.convertToArray(wrapped, type);
        return new ArrayFrpcCallResult<>((T[]) conversionResult.getConverted(), httpResponseStatus);
    }

}
