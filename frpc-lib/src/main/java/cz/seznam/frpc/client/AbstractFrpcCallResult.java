package cz.seznam.frpc.client;

import cz.seznam.frpc.core.ConversionResult;
import cz.seznam.frpc.core.FrpcType;
import cz.seznam.frpc.core.FrpcTypesConverter;
import cz.seznam.frpc.core.transport.FrpcFault;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
abstract class AbstractFrpcCallResult<T> {

    final T wrapped;
    final int httpResponseStatus;

    AbstractFrpcCallResult(T wrapped, int httpResponseStatus) {
        this.wrapped = wrapped;
        this.httpResponseStatus = httpResponseStatus;
    }

    public boolean isStruct() {
        return !isNull() && wrapped instanceof Map;
    }

    public boolean isArray() {
        return !isNull() && wrapped.getClass().isArray();
    }

    public boolean isFault() {
        return !isNull() && wrapped instanceof FrpcFault;
    }

    public boolean isNull() {
        return wrapped == null;
    }

    public Object asObject() {
        return wrapped;
    }

    public int getHttpResponseStatus() {
        return httpResponseStatus;
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
        }
        // if none of the above worked, then we can't cast the value to required type
        throw new ClassCastException(
                "Object of type " + getWrappedType() + " cannot be cast to " + type + ". Cause: " + result
                        .getErrorMessage());
    }

    public <U> U mapTo(Function<T, U> mapper) {
        return Objects.requireNonNull(mapper, "Given mapping function must not be null").apply(wrapped);
    }

    protected Class<?> getWrappedType() {
        return wrapped == null ? Void.class : wrapped.getClass();
    }

}
