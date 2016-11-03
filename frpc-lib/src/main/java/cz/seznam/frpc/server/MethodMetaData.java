package cz.seznam.frpc.server;

import cz.seznam.frpc.server.annotations.FrpcResponse;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class MethodMetaData {

    private Class<?>[] parameterTypes;
    private Class<?> returnType;
    private String responseKey;

    public MethodMetaData(Class<?>[] parameterTypes, Class<?> returnType, String responseKey) {
        this.parameterTypes = Objects.requireNonNull(parameterTypes);
        this.returnType = Objects.requireNonNull(returnType);
        this.responseKey = responseKey;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public String getResponseKey() {
        return responseKey;
    }

    public static MethodMetaData fromMethod(Method method) {
        Objects.requireNonNull(method, "Method must not be null");

        // getResult FrpcResponse annotation of this method
        FrpcResponse frpcResponse = method.getAnnotation(FrpcResponse.class);
        // and try to getResult responseKey value out of it
        String responseKey = frpcResponse == null ? null : frpcResponse.key();
        // create MethodMetaData and return it
        return new MethodMetaData(method.getParameterTypes(), method.getReturnType(), responseKey);
    }

}
