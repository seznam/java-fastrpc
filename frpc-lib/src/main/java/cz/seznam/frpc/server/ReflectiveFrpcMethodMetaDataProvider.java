package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Implementation of {@link FrpcMethodMetaDataProvider} using reflection to obtain meta data for methods.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveFrpcMethodMetaDataProvider extends CachingFrpcMethodMetaDataProvider {

    /**
     * Method locator used to find Java methods by names of corresponding {@code FRPC} methods.
     */
    private FrpcHandlerMethodLocator methodLocator;

    /**
     * Creates new instance using given {@link FrpcHandlerMethodLocator} to find methods by their names.
     *
     * @param methodLocator method locator used to find Java methods by their names.
     */
    ReflectiveFrpcMethodMetaDataProvider(FrpcHandlerMethodLocator methodLocator) {
        this.methodLocator = Objects.requireNonNull(methodLocator);
    }

    @Override
    protected FrpcMethodMetaData doGetMethodMetaData(String methodFrpcName) throws NoSuchMethodException {
        // try to find the right method by value
        Method method = methodLocator.locateMethodByFrpcName(methodFrpcName);
        // return method metadata
        return FrpcMethodMetaData.fromMethod(method);
    }

}
