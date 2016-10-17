package cz.seznam.frpc.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveFrpcHandlerWrapper implements FrpcHandler {

    private Supplier<?> supplier;
    private Map<String, Method> methods;

    public ReflectiveFrpcHandlerWrapper(Supplier<?> supplier, Map<String, Method> methods) {
        this.supplier = supplier;
        this.methods = methods;
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
        Method nameMatchingMethod = methods.get(methodName);
        // if there is no mapping for such a method name, throw an exception
        if(nameMatchingMethod == null) {
            throw new NoSuchMethodException("No method found for method name " + methodName);
        }
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
