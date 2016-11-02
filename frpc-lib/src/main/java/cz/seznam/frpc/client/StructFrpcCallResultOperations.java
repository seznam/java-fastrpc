package cz.seznam.frpc.client;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface StructFrpcCallResultOperations<T extends AbstractFrpcCallResult, S extends AbstractFrpcCallResult> {

    public Map<String, Object> asMap();

    public T get(String key);

    public S getStruct(String key);

}
