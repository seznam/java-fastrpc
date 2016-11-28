package cz.seznam.frpc.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * {@code FrpcType} serves as type reference for type-safe deserialization of values.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class FrpcType<T> {

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
     * Returns generic type of this {@code FrpcType} instance as {@link Type}.
     *
     * @return generic type of this {@code FrpcType} instance as {@link Type}.
     */
    public Type getGenericType() {
        return type;
    }

}
