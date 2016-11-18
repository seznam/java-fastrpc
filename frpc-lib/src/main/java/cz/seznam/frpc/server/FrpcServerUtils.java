package cz.seznam.frpc.server;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods for {@code FRPC} server.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcServerUtils.class);

    private static final Object CANNOT_CONVERT = new Object();

    /**
     * Convenience method for calling {@link #addDefaultFrpcHandler(Server, String, FrpcHandlerMapping, boolean)} with
     * {@code true} as the last argument.
     *
     * @param server server to add the default request handler to
     * @param contextPath context path to map the handler to
     * @param handlerMapping handler mapping used to initialize an instance of {@code HandlerUsingFrpcRequestProcesor}
     *                       used by the default request handler
     */
    public static void addDefaultFrpcHandler(Server server, String contextPath, FrpcHandlerMapping handlerMapping) {
        addDefaultFrpcHandler(server, contextPath, handlerMapping, true);
    }

    /**
     * Adds default {@code FRPC} handler (which is {@link FrpcRequestHandler}) to given {@code Server}. The
     * {@code FrpcRequestHandler} is initialized with new instance of {@link HandlerUsingFrpcRequestProcesor} using
     * given {@code handlerMapping}. <br />
     * Newly created {@code FrpcRequestHandler} is then wrapped by context handler using
     * {@link #createContextHandler(String, Handler, boolean)} before being set to the server.
     *
     * @param server server to add the default request handler to
     * @param contextPath context path to map the handler to
     * @param handlerMapping handler mapping used to initialize an instance of {@code HandlerUsingFrpcRequestProcesor}
     *                       used by the default request handler
     * @param allowNullPathsInfo if {@code true} then no redirect is done by the server in case it encounters an
     *                           absolute URL matching given {@code contextPath} and not ending with a trailing slash;
     *                           if {@code false} then redirect to the URL with added trailing slash is done by the
     *                           server
     */
    public static void addDefaultFrpcHandler(Server server, String contextPath, FrpcHandlerMapping handlerMapping,
                                             boolean allowNullPathsInfo) {
        // create default handler
        Handler handler = new FrpcRequestHandler(new HandlerUsingFrpcRequestProcesor(handlerMapping));
        // map the handler to given context path
        handler = createContextHandler(contextPath, handler, allowNullPathsInfo);
        // set it to the server
        server.setHandler(handler);
    }

    /**
     * Convenience method for calling {@link #createContextHandler(String, Handler, boolean)} with {@code true} as the
     * last argument.
     *
     * @param contextPath context path to map the handler to
     * @param handler handler to delegate to through the newly created {@code ContextHandler}
     * @return either new instance of {@link ContextHandler} wrapping given {@code handler} and mapped to given
     *         {@code contextPath} or given {@code handler} unmodified (if the {@code contextPath} is blank)
     */
    public static Handler createContextHandler(String contextPath, Handler handler) {
        return createContextHandler(contextPath, handler, true);
    }

    /**
     * Creates an instance of {@link ContextHandler} from given {@code contextPath}. Given {@code handler} is set as the
     * underlying handler for the newly created {@code ContextHandler} instance. <br />
     * If given {@code contextPath} is {@code null} or empty, then given {@code handler} is returned as is.
     *
     * @param contextPath context path to map the handler to
     * @param handler handler to delegate to through the newly created {@code ContextHandler}
     * @param allowNullPathsInfo if {@code true} then no redirect is done by the server in case it encounters an
     *                           absolute URL matching given {@code contextPath} and not ending with a trailing slash;
     *                           if {@code false} then redirect to the URL with added trailing slash is done by the
     *                           server
     * @return either new instance of {@link ContextHandler} wrapping given {@code handler} and mapped to given
     *         {@code contextPath} or given {@code handler} unmodified (if the {@code contextPath} is blank)
     */
    public static Handler createContextHandler(String contextPath, Handler handler, boolean allowNullPathsInfo) {
        if(StringUtils.isBlank(contextPath)) {
            return handler;
        }
        ContextHandler contextHandler = new ContextHandler(contextPath);
        contextHandler.setHandler(handler);
        contextHandler.setAllowNullPathInfo(allowNullPathsInfo);
        return contextHandler;
    }

    public static Object[] checkAndConvertMethodParameters(String requestMethodName, Class<?>[] methodParameterTypes,
                                                           Object[] parameters) {
        LOGGER.debug("Trying to convert arguments {} given to method \"{}\" to these parameter types: {}",
                parameters, requestMethodName, methodParameterTypes);

        // check if there is at least as many arguments as their types
        if(methodParameterTypes.length < parameters.length) {
            LOGGER.warn("Too many arguments given to method \"{}\", {} arguments required but {} given. Ignoring " +
                    "superfluous {} parameters.", requestMethodName, methodParameterTypes.length, parameters.length,
                    methodParameterTypes.length - parameters.length);
        } else if(methodParameterTypes.length > parameters.length) {
            LOGGER.error("Too few arguments given to method \"{}\", {} arguments required but only these {} " +
                            "arguments" + " were given: {}", requestMethodName, methodParameterTypes.length,
                    parameters.length, parameters);
            throw new IllegalArgumentException("Too few arguments given to method \"" + requestMethodName + "\", " +
                    methodParameterTypes.length + " required but only " + parameters.length + " given.");
        }

        // try to convert all parameters to given types
        Object[] arguments = new Object[methodParameterTypes.length];
        for(int i = 0; i < arguments.length; i++) {
            // if the argument is null, just store it in the arguments array
            arguments[i] = convertParameter(requestMethodName, methodParameterTypes, i, parameters[i]);
        }
        // return converted arguments
        return arguments;
    }

    private static Object convertParameter(String requestMethodName, Class<?>[] methodParameterTypes, int i,
                                           Object parameter) {
        // try to convert the argument into something compatible with current parameter type
        Object convertedArgument = convertToCompatibleInstance(methodParameterTypes[i], parameter);
        // if we cannot convert the parameter into a value compatible with required type, throw an exception
        if(convertedArgument == CANNOT_CONVERT) {
            throw new IllegalArgumentException(
                    "Error while reading method arguments. "
                            + getMethodDescription(requestMethodName, methodParameterTypes)
                            + " Argument #" + (i + 1) + " is then expected to be "
                            + methodParameterTypes[i].getSimpleName() + " but "
                            + (parameter == null ? "null" : " of type " + parameter.getClass().getSimpleName())
                            + " was given");
        }
        // add it to the array of parameters
        LOGGER.debug("Setting {} as argument #{}", convertedArgument, i + 1);
        return convertedArgument;
    }

    private static String getMethodDescription(String methodName, Class<?>[] parameterTypes) {
        return "FRPC method \"" + methodName + "\" is mapped to handler method with parameters of types "
                + Arrays.stream(parameterTypes).map(Class::getSimpleName).collect(Collectors.joining(",", "[", "]"))
                + ".";
    }

    private static Object convertToCompatibleInstance(Class<?> methodParameterType, Object parameter) {
        LOGGER.debug("Trying to convert argument {} into something compatible with {}", parameter, methodParameterType);
        // if the argument is null
        if(parameter == null) {
            // it will be compatible with any type except for primitives
            return methodParameterType.isPrimitive() ? CANNOT_CONVERT : null;
        }
        // getResult boxed method parameter type
        Class<?> boxedMethodParameterType = ClassUtils.primitiveToWrapper(methodParameterType);
        // check if argument is instance of given type or if they are compatible numbers
        if(boxedMethodParameterType.isInstance(parameter)) {
            LOGGER.debug("Argument {} is type-compatible with required method parameter type {}, no conversion needed.",
                    parameter, methodParameterType);
            return parameter;
        }
        // getResult boxed type of the argument
        Class<?> boxedParameterType = ClassUtils.primitiveToWrapper(parameter.getClass());
        // check for compatible integer and floating point types
        if((boxedMethodParameterType == Long.class && boxedParameterType == Integer.class)
                || (boxedMethodParameterType == Double.class && boxedParameterType == Float.class)) {
            LOGGER.debug("Argument {} is numeric type with lower precision than method parameter type {}, relying on"
                    + " implicit widening conversion.", parameter, methodParameterType);
            return parameter;
        }
        // check if both classes are array types
        if(boxedMethodParameterType.isArray() && boxedParameterType.isArray()) {
            // if they are, try to convert them into compatible types
            LOGGER.debug("Both argument type and method argument type are array types, trying to create an array that" +
                    " will work");
            // create new array of desired type
            Class<?> arrayType = boxedMethodParameterType.getComponentType();
            Object newArray = Array.newInstance(arrayType, Array.getLength(parameter));
            // iterate all elements of given array
            for (int i = 0; i < Array.getLength(parameter); i++) {
                // try to convert object in given array at current index into something compatible with desired type
                Object converted = convertToCompatibleInstance(arrayType, Array.get(parameter, i));
                // if it could be converted, store it in the new array
                if(converted != CANNOT_CONVERT) {
                    Array.set(newArray, i, converted);
                } else {
                    // otherwise this parameter could not be converted, return CANNOT_CONVERT
                    return CANNOT_CONVERT;
                }
            }
            // all elements converted successfully, return new array
            return newArray;
        }
        // if the method parameter type is a list and argument is an array, convert it into list
        if(boxedMethodParameterType == List.class && boxedParameterType == Object[].class) {
            LOGGER.debug("Argument {} is an object array, converting it into List.", parameter, methodParameterType);
            return Arrays.stream((Object[]) parameter).collect(Collectors.toList());
        }
        // if the method parameter type is a set and argument is an array, convert it into list
        if(boxedMethodParameterType == Set.class && boxedParameterType == Object[].class) {
            LOGGER.debug("Argument {} is an object array, converting it into Set.", parameter, methodParameterType);
            return Arrays.stream((Object[]) parameter).collect(Collectors.toSet());
        }
        // if none of the above is true, given argument is not compatible with given type
        return CANNOT_CONVERT;
    }

}
