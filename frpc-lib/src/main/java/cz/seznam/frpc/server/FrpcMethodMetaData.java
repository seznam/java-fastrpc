package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple DTO holding meta data about Java {@link Method} implementing a {@code FRPC} method.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcMethodMetaData {

    /**
     * Array of parameter types of the method.
     */
    private Type[] parameterTypes;
    /**
     * Return type of the method.
     */
    private Type returnType;
    /**
     * Key to store the result of this method under in the result map.
     */
    private Map<String, Object> additionalData;

    /**
     * Creates new instance from given arguments.
     *
     * @param parameterTypes array of parameter types of the method
     * @param returnType     return type of the method
     * @param additionalData
     */
    public FrpcMethodMetaData(Type[] parameterTypes, Type returnType, Map<String, Object> additionalData) {
        this.parameterTypes = Objects.requireNonNull(parameterTypes);
        this.returnType = Objects.requireNonNull(returnType);
        this.additionalData = additionalData == null ? Collections.emptyMap() : Collections
                .unmodifiableMap(new HashMap<>(additionalData));
    }

    /**
     * Returns parameter types of the method.
     *
     * @return parameter types of the method
     */
    Type[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Returns result type of the method.
     *
     * @return result type of the method
     */
    Type getReturnType() {
        return returnType;
    }

    /**
     * Returns an unmodifiable map containing any additional meta data provided {@link FrpcMethodResolver} which
     * resolved the method represented by this object.
     *
     * @return an immutable map of additional meta data, may be empty, is never {@code null}
     */
    Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    /**
     * Reads meta data of given method and creates an instance of {@code FrpcMethodMetaData} out of them.
     *
     * @param method method to read meta data from
     * @return an instance of {@code FrpcMethodMetaData} containing meta data read from given method
     */
    static FrpcMethodMetaData fromMethod(Method method) {
        Objects.requireNonNull(method, "Method must not be null");
        // create FrpcMethodMetaData and return it
        return new FrpcMethodMetaData(method.getGenericParameterTypes(), method.getGenericReturnType(), null);
    }

    /**
     * Reads meta data of given method and creates an instance of {@code FrpcMethodMetaData} out of them.
     *
     * @param method         method to read meta data from
     * @param additionalData map of any additional meta data provided by {@link FrpcMethodResolver} which resolved the
     *                       method represented by the object being constructed
     * @return an instance of {@code FrpcMethodMetaData} containing meta data read from given method
     */
    static FrpcMethodMetaData fromMethodWithAdditionalData(Method method, Map<String, Object> additionalData) {
        Objects.requireNonNull(method, "Method must not be null");
        // create FrpcMethodMetaData and return it
        return new FrpcMethodMetaData(method.getGenericParameterTypes(), method.getGenericReturnType(), additionalData);
    }

}
