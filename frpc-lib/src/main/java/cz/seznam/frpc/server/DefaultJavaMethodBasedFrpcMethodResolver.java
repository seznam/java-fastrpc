package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 *
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class DefaultJavaMethodBasedFrpcMethodResolver extends AbstractJavaMethodBasedFrpcMethodResolver {

    @Override
    protected Map<String, Object> resolveAdditionalMetaData(Method method) {
        return Collections.emptyMap();
    }

}
