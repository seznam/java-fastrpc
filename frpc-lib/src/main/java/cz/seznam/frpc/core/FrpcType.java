package cz.seznam.frpc.core;

import cz.seznam.frpc.client.FrpcCallResult;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * {@code FrpcType} serves as type reference for type-safe deserialization of values. Typical usage is subclassing this
 * class and passing it to methods of {@link FrpcCallResult}. All instances of this class are inherently immutable so it
 * is safe (and recommended) to reuse them.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class FrpcType<T> {

    /**
     * {@code FRPC} "struct" type which maps to {@code Map<String, Object>} in Java.
     */
    public static final FrpcType<Map<String, Object>> STRUCT = new FrpcType<Map<String, Object>>() {
    };

    private final Type type;

    protected FrpcType() {
        Type superType = this.getClass().getGenericSuperclass();
        if(superType instanceof Class) {
            throw new IllegalArgumentException(
                    "Type parameters must be specified");
        } else {
            type = ((ParameterizedType) superType).getActualTypeArguments()[0];
        }
    }

    /**
     * Returns the actual type argument of this {@code FrpcType} instance as {@link Type}.
     *
     * @return the actual type argument of this {@code FrpcType} instance as {@link Type}
     */
    public Type getGenericType() {
        return type;
    }

}
