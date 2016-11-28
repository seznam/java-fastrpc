package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link AbstractJavaMethodBasedFrpcMethodResolver} using Java Reflection to find {@code FRPC}
 * methods provided by given class.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveFrpcMethodResolver extends AbstractJavaMethodBasedFrpcMethodResolver {

    /**
     * Method locator used to find Java methods by names of corresponding {@code FRPC} methods.
     */
    private ReflectiveFrpcMethodLocator methodLocator;

    ReflectiveFrpcMethodResolver(ReflectiveFrpcMethodLocator methodLocator) {
        this.methodLocator = Objects.requireNonNull(methodLocator);
    }

    /**
     * Uses {@link ReflectiveFrpcMethodLocator} to discover Java methods declared by given class which may
     * represent a {@code FRPC} method implementation. <br />
     * For each of them, calls {@link #resolveAdditionalMetaData(Method)} (which by default returns an empty map,
     * but can be overridden by subclasses). <br />
     * Finally, an instance of {@link FrpcMethodMetaData} is constructed for each {@code FRPC} method via
     * {@link FrpcMethodMetaData#fromMethodWithAdditionalData(Method, Map)} using the method and additional meta data.
     *
     * @param examined class to be examined
     * @return a map mapping {@code FRPC} method names to their descriptions in form of {@code FrpcMethodMetaData}
     */
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

    /**
     * Does nothing, that is returns an empty map for any input argument.
     *
     * @param method method to resolve additional meta data for
     *
     * @return an empty map, always
     */
    @Override
    protected Map<String, Object> resolveAdditionalMetaData(Method method) {
        return Collections.emptyMap();
    }
}
