package cz.seznam.frpc.server;

import cz.seznam.frpc.common.FrpcUnmarshaller;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcServerUtils.class);

    public static void addDefaultFrpcHandler(Server server, String contextPath, FrpcHandlerMapping handlerMapping) {
        addDefaultFrpcHandler(server, contextPath, handlerMapping, true);
    }

    public static void addDefaultFrpcHandler(Server server, String contextPath, FrpcHandlerMapping handlerMapping, boolean allowNullPathsInfo) {
        // create default handler
        Handler handler = new FrpcRequestHandler(new HandlerUsingFrpcRequestProcesor(handlerMapping));
        // map the handler to given context path
        handler = createContextHandler(contextPath, handler, allowNullPathsInfo);
        // set it to the server
        server.setHandler(handler);
    }

    public static Handler createContextHandler(String contextPath, Handler handler) {
        return createContextHandler(contextPath, handler, true);
    }

    public static Handler createContextHandler(String contextPath, Handler handler, boolean allowNullPathsInfo) {
        if(StringUtils.isBlank(contextPath)) {
            return handler;
        }
        ContextHandler contextHandler = new ContextHandler(contextPath);
        contextHandler.setHandler(handler);
        contextHandler.setAllowNullPathInfo(allowNullPathsInfo);
        return contextHandler;
    }

    public static Object[] unmarshallMethodArguments(String requestMethodName, Class<?>[] methodParameterTypes, FrpcUnmarshaller unmarshaller) {
        LOGGER.debug("Trying to unmarshall arguments of method \"{}\" according to these parameter types: {}", requestMethodName, methodParameterTypes);
        // try to unmarshall as many objects as there are argument types
        Object[] arguments = new Object[methodParameterTypes.length];
        for(int i = 0; i < arguments.length; i++) {
            // try to unmarshall one parameter
            Object argument;
            try {
                argument = unmarshaller.unmarshallObject();
                LOGGER.debug("Unmarshalled {} as method argument #{}", argument, i + 1);
            } catch (Exception e) {
                throw new IllegalStateException(getMethodDescription(requestMethodName, methodParameterTypes) + " There was an error while reading parameter #" + (i + 1), e);
            }
            // if the argument is null, just store it in the arguments array
            if(argument == null) {
                arguments[i] = null;
                LOGGER.debug("Setting null as argument #{}", i + 1);
            } else {
                // otherwise try to convert the argument into something compatible with current parameter type
                Object convertedArgument = convertToCompatibleInstance(methodParameterTypes[i], argument);
                if(convertedArgument == null) {
                    throw new IllegalArgumentException(
                            "Error while reading method arguments. " + getMethodDescription(requestMethodName, methodParameterTypes)
                                    + " Argument no. " + (i + 1) + " is then expected to be " + methodParameterTypes[i].getSimpleName() + " but an object of type "
                                    + argument.getClass().getSimpleName() + " was given");
                }
                // add it to the array of parameters
                arguments[i] = convertedArgument;
                LOGGER.debug("Setting {} as argument #{}", convertedArgument, i + 1);
            }
        }
        // return unmarshalled arguments
        return arguments;
    }

    private static String getMethodDescription(String methodName, Class<?>[] parameterTypes) {
        return "Method \"" + methodName + "\" is mapped to handler method with parameters of types "
                + Arrays.stream(parameterTypes).map(Class::getSimpleName).collect(Collectors.joining(",", "[", "]"))
                + ".";
    }

    private static Object convertToCompatibleInstance(Class<?> methodParameterType, Object argument) {
        LOGGER.debug("Trying to convert argument {} into something compatible with {}", argument, methodParameterType);
        // getResult boxed method parameter type
        Class<?> boxedMethodParameterType = ClassUtils.primitiveToWrapper(methodParameterType);
        // check if argument is instance of given type or if they are compatible numbers
        if(boxedMethodParameterType.isInstance(argument)) {
            LOGGER.debug("Argument {} is type-compatible with required method parameter type {}, no conversion needed.", argument, methodParameterType);
            return argument;
        }
        // getResult boxed type of the argument
        Class<?> boxedArgumentType = ClassUtils.primitiveToWrapper(argument.getClass());
        // check for compatible integer and floating point types
        if((boxedMethodParameterType == Long.class && boxedArgumentType == Integer.class)
                || (boxedMethodParameterType == Double.class && boxedArgumentType == Float.class)) {
            LOGGER.debug("Argument {} is numeric type with lower precision than method parameter type {}, relying on implicit widening conversion.", argument, methodParameterType);
            return argument;
        }
        // if the method parameter type is a list and argument is an array, convert it into list
        if(boxedMethodParameterType == List.class && boxedArgumentType == Object[].class) {
            LOGGER.debug("Argument {} is an object array, converting it into List.", argument, methodParameterType);
            return Arrays.stream((Object[]) argument).collect(Collectors.toList());
        }
        // if the method parameter type is a set and argument is an array, convert it into list
        if(boxedMethodParameterType == Set.class && boxedArgumentType == Object[].class) {
            LOGGER.debug("Argument {} is an object array, converting it into Set.", argument, methodParameterType);
            return Arrays.stream((Object[]) argument).collect(Collectors.toSet());
        }
        // if none of the above is true, given argument is not compatible with given type
        return null;
    }

}
