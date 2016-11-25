package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveFrpcMethodResolver extends DefaultJavaMethodBasedFrpcMethodResolver {

    /**
     * Method locator used to find Java methods by names of corresponding {@code FRPC} methods.
     */
    private ReflectiveFrpcHandlerMethodLocator methodLocator;

    ReflectiveFrpcMethodResolver(ReflectiveFrpcHandlerMethodLocator methodLocator) {
        this.methodLocator = Objects.requireNonNull(methodLocator);
    }

    public Map<String, FrpcMethodMetaData> resolveFrpcMethods(Class examined) {
        // get all methods from the locator
        Map<String, Method> methods = methodLocator.getMethodsByName();
        // construct output map
        Map<String, FrpcMethodMetaData> result = new HashMap<>(methods.size());

        // for each method
        for(Map.Entry<String, Method> entry : methods.entrySet()) {
            // resolve additional data
            Map<String, Object> additionalData = resolveAdditionalMetaData(entry.getValue());
            // create meta data holder
            FrpcMethodMetaData metaData = FrpcMethodMetaData
                    .fromMethodWithAdditionalData(entry.getValue(), additionalData);
            // add it to the output map
            result.put(entry.getKey(), metaData);
        }
        // return the result
        return result;
    }

}
