package cz.seznam.frpc.client;

import cz.seznam.frpc.core.ConversionResult;
import cz.seznam.frpc.core.FrpcType;
import cz.seznam.frpc.core.FrpcTypesConverter;
import cz.seznam.frpc.core.transport.FrpcFault;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcCallResult extends AbstractFrpcCallResult<Object> {

    FrpcCallResult(Object wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    @SuppressWarnings("unchecked")
    public <U> U as(Class<U> type) {
        return (U) as((Type) Objects.requireNonNull(type, "Type must not be null"));
    }

    @SuppressWarnings("unchecked")
    public <U> U as(FrpcType<U> type) {
        return (U) as(Objects.requireNonNull(type, "Type must not be null").getGenericType());
    }

    private Object as(Type type) {
        // try to convert value to type described by given generic type
        ConversionResult result = FrpcTypesConverter.safeConvertToCompatibleInstance(wrapped, type);
        // if the conversion was successful, return the result
        if (result.isSuccess()) {
            return result.getConverted();
        } else {
            // the object could not be converted, check if it is a FrpcFault
            if(isFault()) {
                throw new FrpcFaultException((FrpcFault) wrapped);
            }
        }
        // if none of the above worked, then we can't cast the value to required type
        throw new ClassCastException(
                "Object of type " + getWrappedType() + " cannot be cast to " + type + ". Cause: " + result
                        .getErrorMessage());
    }

    public FrpcFault asFault() {
        if (!isFault()) {
            throw new IllegalStateException(
                    "Cannot convert this FRPC result to FRPC fault form since the object wrapped by this instance is " +
                            "not a fault, it is a " + getWrappedType());
        }
        return as(FrpcFault.class);
    }

    @SuppressWarnings("unchecked")
    public StructFrpcCallResult asStruct() {
        if (isFault() || !isStruct()) {
            throw new IllegalStateException(
                    "Cannot convert this FRPC result to structured form since the object wrapped by this instance is " +
                            "not a map, it is a " + getWrappedType());
        }
        return new StructFrpcCallResult(as(FrpcType.STRUCT), httpResponseStatus);
    }

    public ArrayFrpcCallResult<Object> asObjectArray() {
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
    public <T> ArrayFrpcCallResult<T> asArrayOf(Type type) {
        if (isFault() || !isArray()) {
            throw new IllegalStateException(
                    "Cannot convert this FRPC result to array form since the object wrapped by this instance is not " +
                            "an array, it is a " + getWrappedType());
        }
        ConversionResult conversionResult = FrpcTypesConverter.convertToArray(wrapped, type);

        return new ArrayFrpcCallResult<>((T[]) conversionResult.getConverted(), httpResponseStatus);
    }

}
