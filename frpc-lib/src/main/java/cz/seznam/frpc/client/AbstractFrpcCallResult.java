package cz.seznam.frpc.client;

import org.apache.commons.lang3.ClassUtils;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
abstract class AbstractFrpcCallResult<T> {

    protected final T wrapped;
    protected final int httpResponseStatus;

    AbstractFrpcCallResult(T wrapped, int httpResponseStatus) {
        this.wrapped = wrapped;
        this.httpResponseStatus = httpResponseStatus;
    }

    public boolean isStruct() {
        return wrapped instanceof Map;
    }

     public boolean isArray() {
        return wrapped instanceof Object[];
    }

    public T asObject() {
        return wrapped;
    }

    public int getHttpResponseStatus() {
        return httpResponseStatus;
    }

    @SuppressWarnings("unchecked")
    public <X> X as(Class<X> type) {
        // check if the object is directly an instance of given type
        if(wrapped == null || type.isInstance(wrapped)) {
            // if it is, cast it and return it
            return type.cast(wrapped);
        }
        // if it's not, check for special cases we can convert
        Class<?> boxedType = ClassUtils.primitiveToWrapper(type);
        Class<?> boxedWrappedType = ClassUtils.primitiveToWrapper(wrapped.getClass());
        // like integer types
        if(boxedType == Long.class && boxedWrappedType == Integer.class) {
            return type.cast(((Integer) wrapped).longValue());
        }
        // or floating-point types
        if(boxedType == Double.class && boxedWrappedType == Float.class) {
            return type.cast(((Float) wrapped).doubleValue());
        }
        // if none of the above worker, then we can't cast the value to required type
        throw new ClassCastException("Object of type " + getWrappedType() + " cannot be cast to " + type);
    }

    public <U> U mapTo(Function<T, U> mapper) {
        return Objects.requireNonNull(mapper).apply(wrapped);
    }

    protected Class<?> getWrappedType() {
        return wrapped == null ? Void.class : wrapped.getClass();
    }

}
