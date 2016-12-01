package cz.seznam.frpc.client;

import cz.seznam.frpc.core.ConversionResult;
import cz.seznam.frpc.core.FrpcType;
import cz.seznam.frpc.core.FrpcTypesConverter;
import cz.seznam.frpc.core.transport.FrpcFault;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a {@code FRPC} call result. Provides convenience methods for checking what kind of object was returned and
 * fluent API methods for converting it into desired type.
 *
 * @param <T> type of the object wrapped by this {@code FRPC} call result
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcCallResult<T> extends AbstractFrpcCallResult<T> {

    FrpcCallResult(T wrapped, int httpResponseStatus) {
        super(wrapped, httpResponseStatus);
    }

    /**
     * Checks whether or not the method result is a "structure", that is checks for the object being a {@link Map}
     * instance.
     * <p>
     * <strong>Notice:</strong> even when this method returns {@code true}, it's not guaranteed that {@link #asStruct()}
     * will actually return without throwing an exception. The problem is that this method only checks for the wrapped
     * object being a {@link Map} instance whereas the {@link #asStruct()} method actually does a type-safe conversion
     * to {@link FrpcType#STRUCT} which is a {@code Map} mapping <strong>strings</strong> to objects. Therefore if it
     * happened somehow that the wrapped object is a map with a non-string key, then this method would return
     * {@code true} and yet the other method would throw an exception. That is why this method is called <i>isMap</i>
     * and not <i>isStruct</i> since it cannot reliably detect the latter.
     *
     * @return {@code true} if the wrapped object is a {@code Map} and false otherwise
     * @see #asStruct()
     */
    public boolean isMap() {
        return !isNull() && wrapped instanceof Map;
    }

    /**
     * Checks whether or not the method result is an array.
     *
     * @return {@code true} if the wrapped object is an array and false otherwise
     */
    public boolean isArray() {
        return !isNull() && wrapped.getClass().isArray();
    }

    /**
     * Checks whether or not the method result is a {@link FrpcFault}.
     *
     * @return {@code true} if the wrapped object is a {@code FrpcFault} and false otherwise
     */
    public boolean isFault() {
        return !isNull() && wrapped instanceof FrpcFault;
    }

    /**
     * Tries to convert wrapped object into an instance of given class.
     *
     * @param type type to convert the wrapped object to
     * @return the wrapped object converted to given type
     * @throws ClassCastException if the wrapped object could not be converted to given type
     * @throws FrpcFaultException if the wrapped object could not be converted to given type and it is an instance of
     *                            {@link FrpcFault}
     * @see FrpcTypesConverter#convertToCompatibleInstance(Object, Type)
     */
    @SuppressWarnings("unchecked")
    public <U> U as(Class<U> type) {
        return (U) as((Type) Objects.requireNonNull(type, "Type must not be null"));
    }

    /**
     * Tries to convert wrapped object into an instance of type described by type argument of provided {@code FrpcType}.
     *
     * @param type object used to describe the Java type to convert the wrapped object to
     * @return the wrapped object converted to given type
     * @throws ClassCastException if the wrapped object could not be converted to given type
     * @throws FrpcFaultException if the wrapped object could not be converted to given type and it is an instance of
     *                            {@link FrpcFault}
     * @see FrpcTypesConverter#convertToCompatibleInstance(Object, Type)
     */
    @SuppressWarnings("unchecked")
    public <U> U as(FrpcType<U> type) {
        return (U) as(Objects.requireNonNull(type, "Type must not be null").getGenericType());
    }

    Object as(Type type) {
        // try to convert value to type described by given generic type
        ConversionResult result = FrpcTypesConverter.convertToCompatibleInstance(wrapped, type);
        // if the conversion was successful, return the result
        if (result.isSuccess()) {
            return result.getConverted();
        } else {
            // the object could not be converted, check if it is a FrpcFault
            if (isFault()) {
                throw new FrpcFaultException((FrpcFault) wrapped);
            }
        }
        // if none of the above worked, then we can't cast the value to required type
        throw new ClassCastException(
                "Object of type " + getWrappedType() + " cannot be cast to " + type + ". Cause: " + result
                        .getErrorMessage());
    }

    /**
     * Checks whether the method result {@link #isFault()} and returns it as a {@link FrpcFault} if it is.
     *
     * @return the wrapped object as a {@code FrpcFault} if it is one and throws an exception if it isn't
     * @throws ClassCastException if the wrapped is not an instance of {@code FrpcFault}
     */
    public FrpcFault asFault() {
        if (!isFault()) {
            throw new ClassCastException(
                    "Cannot convert this FRPC result to FRPC fault form since the object wrapped by this instance is " +
                            "not a fault, it is a " + getWrappedType());
        }
        return as(FrpcFault.class);
    }

    /**
     * Checks whether the method result {@link #isMap()} and tries to convert it to {@link FrpcType#STRUCT}.
     *
     * @return instance of {@link StructFrpcCallResult} wrapping a "structure" constructed by converting the object
     * wrapped by this instance to {@link FrpcType#STRUCT}
     * @throws ClassCastException if the wrapped object could not be converted to {@link FrpcType#STRUCT}
     * @throws FrpcFaultException if the wrapped object could not be converted to {@link FrpcType#STRUCT} and it is an
     *                            instance of {@link FrpcFault}
     * @see FrpcTypesConverter#convertToCompatibleInstance(Object, Type)
     */
    @SuppressWarnings("unchecked")
    public StructFrpcCallResult asStruct() {
        if (!isMap()) {
            throw new ClassCastException(
                    "Cannot convert this FRPC result to structured form since the object wrapped by this instance is " +
                            "not a map, it is a " + getWrappedType());
        }
        return new StructFrpcCallResult(as(FrpcType.STRUCT), httpResponseStatus);
    }

    /**
     * Convenience method for calling {@link #asArrayOf(Class)} with {@code Object.class} as the parameter.
     *
     * @return result of method {@link #asArrayOf(Class)} called with {@code Object.class} as the parameter
     */
    public ArrayFrpcCallResult<Object> asObjectArray() {
        return asArrayOf(Object.class);
    }

    /**
     * Checks whether the method result {@link #isArray()} and tries to convert it to array of objects of given type.
     *
     * @param type desired component type of the array
     * @return the wrapped object converted to array with desired component type
     * @throws ClassCastException if the wrapped object could not be converted into an array of desired type
     * @throws FrpcFaultException if the wrapped object could not be converted into an array of desired type and it is
     *                            an instance of {@link FrpcFault}
     * @see FrpcTypesConverter#convertToCompatibleInstanceOrThrowException(Object, Type)
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayFrpcCallResult<T> asArrayOf(Class<T> type) {
        return (ArrayFrpcCallResult<T>) asArrayOf((Type) type);
    }

    /**
     * Checks whether the method result {@link #isArray()} and tries to convert it to array of objects of given type.
     *
     * @param type desired component type of the array
     * @return the wrapped object converted to array with desired component type
     * @throws ClassCastException if the wrapped object could not be converted into an array of desired type
     * @throws FrpcFaultException if the wrapped object could not be converted into an array of desired type and it is
     *                            an instance of {@link FrpcFault}
     * @see FrpcTypesConverter#convertToCompatibleInstanceOrThrowException(Object, Type)
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayFrpcCallResult<T> asArrayOf(FrpcType<T> type) {
        return (ArrayFrpcCallResult<T>) asArrayOf(type.getGenericType());
    }

    @SuppressWarnings("unchecked")
    <T> ArrayFrpcCallResult<T> asArrayOf(Type type) {
        if (!isArray()) {
            throw new IllegalStateException(
                    "Cannot convert this FRPC result to array form since the object wrapped by this instance is not " +
                            "an array, it is a " + getWrappedType());
        }
        return new ArrayFrpcCallResult<>((T[]) as(TypeUtils.genericArrayType(type)), httpResponseStatus);
    }

}
