package cz.seznam.frpc.client;

import cz.seznam.frpc.common.FrpcResponseUtils;
import org.apache.commons.lang3.ClassUtils;

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
        return wrapped instanceof Map;
    }

    public boolean isArray() {
        return wrapped instanceof Object[];
    }

    public abstract boolean isFrpcError();

    public T asObject() {
        return wrapped;
    }

    public int getHttpResponseStatus() {
        return httpResponseStatus;
    }

    @SuppressWarnings("unchecked")
    public <U> U as(Class<U> type) {
        // if this instance wraps a FRPC error, the only type we can convert it is a Map
        if(isFrpcError() && type != Map.class) {
            throw createError();
        }
        // otherwise try to cast it
        return cast(type);
    }

    @SuppressWarnings("unchecked")
    public <U> U cast(Class<U> type) {
        Objects.requireNonNull(type);
        // check if the object is directly an instance of given type
        if(wrapped == null || type.isInstance(wrapped)) {
            // if it is, cast it and return it
            return (U) wrapped;
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
        // if none of the above worker, then we can't cast the name to required type
        throw new ClassCastException("Object of type " + getWrappedType() + " cannot be cast to " + type);
    }

    public <U> U mapTo(Function<T, U> mapper) {
        return Objects.requireNonNull(mapper).apply(wrapped);
    }

    protected Class<?> getWrappedType() {
        return wrapped == null ? Void.class : wrapped.getClass();
    }

    @SuppressWarnings("unchecked")
    private FrpcErrorResultException createError() {
        // try to get error message
        String errorMessage = null;
        if(this instanceof AbstractUnwrappedFrpcCallResult) {
            errorMessage = ((AbstractUnwrappedFrpcCallResult) this).getStatusMessage();
        } else if(wrapped instanceof Map) {
            errorMessage = (String) ((Map<String, Object>) wrapped).get(FrpcResponseUtils.STATUS_KEY);
        }
        // instantiate FrpcCallException as the cause of the exception we are about to return
        Exception callException = new FrpcCallException(errorMessage);
        // create FrpcErrorResultException
        return new FrpcErrorResultException("Server returned an internal server error.", callException);
    }

}
