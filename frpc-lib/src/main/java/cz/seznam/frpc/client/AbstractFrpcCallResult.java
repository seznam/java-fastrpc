package cz.seznam.frpc.client;

import cz.seznam.frpc.core.FrpcTypes;
import cz.seznam.frpc.core.transport.FrpcFault;
import org.apache.commons.lang3.ClassUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
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
        Objects.requireNonNull(type, "Type must not be null");
        // check for primitive type and null value
        if(wrapped == null && type.isPrimitive()) {
            throw new NullPointerException("Cannot convert null value to type " + type.getName());
        }
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
        // or calendar
        if(wrapped instanceof Calendar) {
            // if date is desired instead
            if(boxedType == Date.class) {
                // just return the date
                return (U) FrpcTypes.calendarToDate(((Calendar) wrapped));
            }
            // if LocalDateTime is needed
            if(boxedType == LocalDateTime.class) {
                // convert Calendar -> Instant -> LocalDateTime
                return (U) FrpcTypes.calendarToLocalDateTime(((Calendar) wrapped));
            }
            // if ZonedDateTime is needed
            if(boxedType == ZonedDateTime.class) {
                // convert Calendar -> Instant -> ZonedDateTime
                return (U) FrpcTypes.calendarToZonedDateTime(((Calendar) wrapped));
            }
        }
        // if none of the above worked, then we can't cast the value to required type
        throw new ClassCastException("Object of type " + getWrappedType() + " cannot be cast to " + type);
    }

    public <U> U mapTo(Function<T, U> mapper) {
        return Objects.requireNonNull(mapper, "Given mapping function must not be null").apply(wrapped);
    }

    protected Class<?> getWrappedType() {
        return wrapped == null ? Void.class : wrapped.getClass();
    }

}
