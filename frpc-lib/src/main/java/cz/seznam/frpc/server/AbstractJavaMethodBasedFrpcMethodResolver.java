package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * A {@link FrpcMethodResolver} which operates on Java {@link Method}s and is capable of resolving additional meta data
 * from a {@code Method} instance.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class AbstractJavaMethodBasedFrpcMethodResolver implements FrpcMethodResolver {

    /**
     * Resolves additional meta data (beyond parameter types and return type) of given method. Additional meta data
     * might be arbitrary.
     *
     * @param method method to resolve additional meta data for
     *
     * @return map of arbitrary additional meta data read from given method
     */
    protected abstract Map<String, Object> resolveAdditionalMetaData(Method method);

}
