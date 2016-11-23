package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class AbstractJavaMethodBasedFrpcMethodResolver implements FrpcMethodResolver {

    protected abstract Map<String, Object> resolveAdditionalData(Method method);

}
