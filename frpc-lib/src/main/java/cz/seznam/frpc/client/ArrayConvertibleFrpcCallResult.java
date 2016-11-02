package cz.seznam.frpc.client;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface ArrayConvertibleFrpcCallResult {

    public default ArrayFrpcCallResult<Object> asArray() {
        return asArrayOf(Object.class);
    }

    public <T> ArrayFrpcCallResult<T> asArrayOf(Class<T> type);

}
