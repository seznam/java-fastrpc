package cz.seznam.frpc.server;

import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public interface FrpcMethodResolver<T> {

    public Map<String, FrpcMethodMetaData> resolveFrpcMethods(Class<T> type);

}
