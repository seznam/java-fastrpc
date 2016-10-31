package cz.seznam.frpc.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveFrpcHandler implements FrpcHandler {

    private Supplier<?> supplier;
    private MethodLocator methodLocator;

    ReflectiveFrpcHandler(Supplier<?> supplier, MethodLocator methodLocator) {
        this.supplier = Objects.requireNonNull(supplier);
        this.methodLocator = Objects.requireNonNull(methodLocator);
    }

    @Override
    public Object handleRequest(String methodName, Object[] args) throws RequestProcessingException {
        try {
            // try to find method matching method name and number of parameters
            Method method = findMethod(methodName, args);
            // try to invoke it
            return method.invoke(supplier.get(), args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RequestProcessingException("Error while processing request to method \"" + methodName
                    + "\" with arguments " + Arrays.toString(args), e);
        }
    }

    private Method findMethod(String methodName, Object[] args) throws NoSuchMethodException {
        // try to find the method by name
        Method nameMatchingMethod = methodLocator.locateMethod(methodName);
        // check that number of given arguments matches signature of given method
        if(nameMatchingMethod.getParameterCount() != args.length) {
            throw new IllegalArgumentException(
                    (nameMatchingMethod.getParameterCount() < args.length ? "Insufficient number of" : "Too many")
                            + " arguments given to method \"" + methodName + "\"");
        }
        // return the method found
        return nameMatchingMethod;
    }

}
