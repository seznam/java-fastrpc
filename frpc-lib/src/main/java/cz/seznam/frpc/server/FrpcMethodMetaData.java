package cz.seznam.frpc.server;

import cz.seznam.frpc.server.annotations.FrpcMethod;

import java.lang.reflect.Method;
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
    private Class<?>[] parameterTypes;
    /**
     * Return type of the method.
     */
    private Class<?> returnType;
    /**
     * Key to store the result of this method under in the result map.
     */
    private String responseKey;

    /**
     * Creates new instance from given arguments.
     *
     * @param parameterTypes array of parameter types of the method
     * @param returnType return type of the method
     * @param responseKey
     */
    public FrpcMethodMetaData(Class<?>[] parameterTypes, Class<?> returnType, String responseKey) {
        this.parameterTypes = Objects.requireNonNull(parameterTypes);
        this.returnType = Objects.requireNonNull(returnType);
        this.responseKey = responseKey;
    }

    /**
     * Returns parameter types of the method.
     *
     * @return parameter types of the method
     */
    Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Returns result type of the method.
     *
     * @return result type of the method
     */
    Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Returns the key to store the result of this method under in the result map. The result may be {@code null} which
     * indicates no key was specified (that is either {@link FrpcMethod} annotation is not present on the method or its
     * {@code resultKey} property is not set).
     *
     * @return key to store the result of this method under in the result map; this key can be {@code null}
     */
    String getResponseKey() {
        return responseKey;
    }

    /**
     * Reads meta data of given method and creates an instance of {@code FrpcMethodMetaData} out of them.
     *
     * @param method method to read meta data from
     * @return an instance of {@code FrpcMethodMetaData} containing meta data read from given method
     */
    static FrpcMethodMetaData fromMethod(Method method) {
        Objects.requireNonNull(method, "Method must not be null");
        // getResult FrpcMethod annotation of this method
        FrpcMethod frpcResponse = method.getAnnotation(FrpcMethod.class);
        // and try to getResult responseKey name out of it
        String responseKey = frpcResponse == null ? null : frpcResponse.resultKey();
        // create FrpcMethodMetaData and return it
        return new FrpcMethodMetaData(method.getParameterTypes(), method.getReturnType(), responseKey);
    }

}
