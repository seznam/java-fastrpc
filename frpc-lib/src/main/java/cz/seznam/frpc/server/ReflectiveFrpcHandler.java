package cz.seznam.frpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveFrpcHandler implements FrpcHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectiveFrpcHandler.class);

    private Supplier<?> supplier;
    private FrpcHandlerMethodLocator methodLocator;

    ReflectiveFrpcHandler(Supplier<?> supplier, FrpcHandlerMethodLocator methodLocator) {
        this.supplier = Objects.requireNonNull(supplier);
        this.methodLocator = Objects.requireNonNull(methodLocator);
    }

    @Override
    public Object handleFrpcMethodCall(String frpcMethodName, Object[] args) throws Exception {
        try {
            // try to find method matching method name and number of parameters
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
        // try to find the method by name
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
