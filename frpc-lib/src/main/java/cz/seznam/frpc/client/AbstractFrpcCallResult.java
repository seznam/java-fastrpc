package cz.seznam.frpc.client;

import cz.seznam.frpc.core.transport.FrpcFault;

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

    public <U> U mapTo(Function<T, U> mapper) {
        return Objects.requireNonNull(mapper, "Given mapping function must not be null").apply(wrapped);
    }

    protected Class<?> getWrappedType() {
        return wrapped == null ? Void.class : wrapped.getClass();
    }

}
