package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveMethodMetaDataProvider implements MethodMetaDataProvider {

    private MethodLocator methodLocator;

    public ReflectiveMethodMetaDataProvider(MethodLocator methodLocator) {
        this.methodLocator = Objects.requireNonNull(methodLocator);
    }

    @Override
    public MethodMetaData getMethodMetaData(String methodName) throws NoSuchMethodException {
        // try to find the right method by name
        Method method = methodLocator.locateMethod(methodName);
        // return method metadata
        return MethodMetaData.fromMethod(method);
    }

}
