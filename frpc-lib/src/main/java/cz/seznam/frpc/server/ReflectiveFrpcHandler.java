package cz.seznam.frpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Implementation of {@link FrpcHandler} using Java Reflection to invoke Java {@link Method}s on objects returned from
 * provided {@link Supplier}.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveFrpcHandler implements FrpcHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectiveFrpcHandler.class);

    /**
     * Supplier providing instance(s) to invoke methods on.
     */
    private Supplier<?> supplier;
    /**
     * Method locator used to find the actual Java method to invoke on single handler instance.
     */
    private FrpcMethodLocator methodLocator;

    /**
     * Creates new instance from given arguments.
     *
     * @param supplier supplier providing instance(s) to invoke methods on
     * @param methodLocator method locator used to find the actual Java method to invoke on single handler instance
     */
    ReflectiveFrpcHandler(Supplier<?> supplier, FrpcMethodLocator methodLocator) {
        this.supplier = Objects.requireNonNull(supplier);
        this.methodLocator = Objects.requireNonNull(methodLocator);
    }

    /**
     * Handles the {@code FRPC} method call by invoking Java method mapped to given {@code frpcMethodName} on object
     * returned by {@code Supplier} given at instantiation time.
     *
     * @param frpcMethodName name of the {@code FRPC} method to call
     * @param args array of arguments for the {@code FRPC} method with given name
     *
     * @return result of method invocation described above
     * @throws Exception if anything goes wrong during method invocation
     */
    @Override
    public Object handleFrpcMethodCall(String frpcMethodName, Object[] args) throws Exception {
        try {
            // try to find method matching method value and number of parameters
            Method method = findMethod(frpcMethodName, args);
            // try to invoke it
            return method.invoke(supplier.get(), args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Error while processing request to method \"{}\" with arguments {}", frpcMethodName,
                    Arrays.toString(args), e);
            throw e;
        }
    }

    private Method findMethod(String methodName, Object[] args) throws NoSuchMethodException {
        // try to find the method by value
        Method nameMatchingMethod = methodLocator.locateMethodByFrpcName(methodName);
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
