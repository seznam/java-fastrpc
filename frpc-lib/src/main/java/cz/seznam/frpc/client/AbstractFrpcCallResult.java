package cz.seznam.frpc.client;

import java.util.Objects;
import java.util.function.Function;

/**
 * Base class representing result of {@code FRPC} method invocation. Wraps an object returned from the {@code FRPC}
 * method and provides simple, yet powerful API for converting it into POJO of desired type.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
abstract class AbstractFrpcCallResult<T> {

    final T wrapped;
    final int httpResponseStatus;

    AbstractFrpcCallResult(T wrapped, int httpResponseStatus) {
        this.wrapped = wrapped;
        this.httpResponseStatus = httpResponseStatus;
    }

    /**
     * Convenience method to check whether the object wrapped by this instance is {@code null}.
     *
     * @return {@code true} if the wrapped object is {@code null} and false otherwise
     */
    public boolean isNull() {
        return wrapped == null;
    }

    /**
     * Returns the object wrapped by this instance. The object is returned as-is, no copying.
     *
     * @return the object wrapped by this instance, as-is
     */
    public T asObject() {
        return wrapped;
    }

    /**
     * Returns the status code of HTTP response which contained the {@code FRPC} method result represented by this
     * object.
     *
     * @return the status code of HTTP response which contained the {@code FRPC} method result represented by this
     * object
     */
    public int getHttpResponseStatus() {
        return httpResponseStatus;
    }

    /**
     * Allows for custom conversion of wrapped object into any other type. Any potential side effects modifying
     * the wrapped object will have permanent effect as the object is given to the mapper function as-is.
     *
     * @param mapper function mapping object wrapped by this instance to object of any type
     * @return result of application of given mapper function to the wrapped object
     */
    public <U> U mapTo(Function<T, U> mapper) {
        return Objects.requireNonNull(mapper, "Given mapping function must not be null").apply(wrapped);
    }

    Class<?> getWrappedType() {
        return wrapped == null ? Void.class : wrapped.getClass();
    }

}
