package cz.seznam.frpc.server;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveMethodNameToParameterTypesMapper implements MethodNameToParameterTypesMapper {

    private MethodLocator methodLocator;

    public ReflectiveMethodNameToParameterTypesMapper(MethodLocator methodLocator) {
        this.methodLocator = Objects.requireNonNull(methodLocator);
    }

    @Override
    public Class<?>[] mapToParameterTypes(String methodName) throws NoSuchMethodException {
        // try to find the right method by name
        Method method = methodLocator.locateMethod(methodName);
        // return array of parameter types of this method
        return method.getParameterTypes();
    }
}
