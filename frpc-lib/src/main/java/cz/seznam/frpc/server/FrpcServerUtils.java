package cz.seznam.frpc.server;

import cz.seznam.frpc.core.FrpcTypes;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Utility methods for {@code FRPC} server.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcServerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcServerUtils.class);

    private static final Object CANNOT_CONVERT = new Object();

    private static class ConversionResult {
        private final boolean success;
        private final String errorMessage;
        private final Object converted;

        public ConversionResult(boolean success, String errorMessage, Object converted) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.converted = converted;
        }
    }

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

    public static Object[] checkAndConvertMethodParameters(String requestMethodName, Type[] methodParameterTypes,
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

    private static Object convertParameter(String requestMethodName, Type[] methodParameterTypes, int i,
                                           Object parameter) {
        // try to convert the argument into something compatible with current parameter type
        ConversionResult conversionResult = convertToCompatibleInstance(parameter, methodParameterTypes[i]);
        // if we cannot convert the parameter into a value compatible with required type, throw an exception
        if(!conversionResult.success) {
            throw new IllegalArgumentException(
                    "Error while reading argument #" + (i + 1) + ", error message: " + conversionResult.errorMessage);
        }
        // add it to the array of parameters
        LOGGER.debug("Setting {} as argument #{}", conversionResult.converted, i + 1);
        return conversionResult.converted;
    }

    private static ConversionResult convertToCompatibleInstance(Object parameter, Type type) {
        // check the method parameter type implementation
        if(type instanceof Class) {
            // it's a simple class, convert it to object right away
            return convertToObject(parameter, (Class<?>) type);
        } else if(type instanceof ParameterizedType) {
            // it's a parameterized type, check it's raw type and parameter arguments
            ParameterizedType parameterizedType = (ParameterizedType) type;
            // check that the raw type is supported
            Class<?> rawType = (Class) parameterizedType.getRawType();
            if(!FrpcTypes.isSupportedRawType(rawType)) {
                return error(rawType);
            }
            // get actual type arguments
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            // do the conversion
            ConversionResult result;
            // check whether it is a map or a collection
            if(FrpcTypes.isSupportedMapType(rawType)) {
                // try to convert it to type-safe map
                result = convertToMap(parameter, rawType, typeArguments[0], typeArguments[1]);
            } else if(FrpcTypes.isSupportedCollectionType(rawType)) {
                // try to convert it to collection
                result = convertToCollection(parameter, rawType, typeArguments[0]);
            } else {
                throw new IllegalStateException("Unsupported parameterized type " + parameterizedType
                        .getTypeName() + " was reported as supported by " + FrpcTypes.class.getSimpleName());
            }
            // if the result is an error, describe it and return an error
            return checkAndDescribeErrorOrReturnSuccess(result, parameter, parameterizedType);
        } else if(type instanceof GenericArrayType) {
            // generic array type
            GenericArrayType genericArrayType = ((GenericArrayType) type);
            // try to convert it to array of given component type
            ConversionResult conversionResult = convertToArray(parameter, genericArrayType.getGenericComponentType());
            // if the result is an error, describe it and return an error
            return checkAndDescribeErrorOrReturnSuccess(conversionResult, parameter, genericArrayType);
        } else if(type instanceof TypeVariable) {
            return error("Type variables are not supported");
        } else if(type instanceof WildcardType) {
            return error("Wildcard types are not supported");
        }
        // generic error
        return error("Unknown Type implementation " + type.getClass().getCanonicalName());
    }

    private static ConversionResult checkAndDescribeErrorOrReturnSuccess(ConversionResult conversionResult,
                                                                         Object parameter, Type type) {
        if (!conversionResult.success) {
            return error("Error while converting value " + parameter + " to " + type
                    .getTypeName(), conversionResult);
        } else {
            return conversionResult;
        }
    }

    @SuppressWarnings("unchecked")
    private static ConversionResult convertToMap(Object parameter, Class<?> mapType,
                                                 Type keysType, Type valuesType) {
        // if the parameter is null, return it right away
        if(parameter == null) {
            return success(null);
        }
        // check that the argument actually is a map
        if(!(parameter instanceof Map)) {
            return error("Cannot convert from " + parameter.getClass()
                    .getCanonicalName() + " to map, incompatible types");
        }
        // cast the parameter to map
        Map<?, ?> parameterAsMap = ((Map) parameter);
        // create the map
        Map map = FrpcTypes.instantiateMap(mapType);
        // iterate all entries in the original map
        for(Map.Entry<?, ?> e : parameterAsMap.entrySet()) {
            // convert key and value
            ConversionResult keyConversionResult = convertToCompatibleInstance(e.getKey(), keysType);
            if(!keyConversionResult.success) {
                return keyConversionResult;
            }
            ConversionResult valueConversionResult = convertToCompatibleInstance(e.getValue(), valuesType);
            if(!valueConversionResult.success) {
                return valueConversionResult;
            }

            // put them to the map
            map.put(keyConversionResult.converted, valueConversionResult.converted);
        }
        // return the map
        return success(map);
    }

    private static ConversionResult convertToArray(Object parameter, Type componentType) {
        // if the parameter is null, return it right away
        if(parameter == null) {
            return success(null);
        }

        // we are expecting an Object[]
        if(parameter.getClass() != Object[].class) {
            return error("Cannot convert from " + parameter.getClass()
                    .getCanonicalName() + " to array, incompatible types");
        }
        // cast the parameter to Object[]
        Object[] parameterAsArray = ((Object[]) parameter);

        // determine component type of the array to be created
        Class componentClass;

        // check if the component type is either a class or a parameterized type and allocate new array properly
        if(componentType instanceof Class) {
            componentClass = ((Class) componentType);
        } else if(componentType instanceof ParameterizedType) {
            componentClass = ((Class) ((ParameterizedType) componentType).getRawType());
        } else {
            return error("Type " + componentType.getTypeName() + " is not supported component array type");
        }

        // get length of the original array
        int length = Array.getLength(parameter);
        // allocate new array of proper length
        Object newArray = Array.newInstance(componentClass, length);
        // iterate all elements of the original array
        for(int i = 0; i < length; i++) {
            // try to convert each of them
            ConversionResult converted = convertToCompatibleInstance(parameterAsArray[i], componentType);
            // if the conversion failed, return an error
            if(!converted.success) {
                return converted;
            }
            // set the converted value into the new array
            Array.set(newArray, i, converted.converted);
        }
        // return newly created array
        return success(newArray);
    }

    @SuppressWarnings("unchecked")
    private static ConversionResult convertToCollection(Object parameter, Class<?> collectionType, Type valuesType) {
        // if the parameter is null, return it right away
        if(parameter == null) {
            return success(null);
        }

        // we are expecting an Object[]
        if(parameter.getClass() != Object[].class) {
            return error("Cannot convert from " + parameter.getClass()
                    .getCanonicalName() + " to collection, incompatible types");
        }
        // cast the parameter to Object[]
        Object[] parameterAsArray = ((Object[]) parameter);

        // instantiate the collection
        Collection collection = FrpcTypes.instantiateCollection(collectionType);

        // get length of the original array
        int length = Array.getLength(parameter);
        // iterate all elements of the original array
        for(int i = 0; i < length; i++) {
            // try to convert each of them
            ConversionResult converted = convertToCompatibleInstance(parameterAsArray[i], valuesType);
            // if the conversion failed, return an error
            if(!converted.success) {
                return converted;
            }
            // set the converted value into the new array
            collection.add(converted.converted);
        }
        // return the collection
        return success(collection);
    }

    private static ConversionResult convertToObject(Object parameter, Class<?> type) {
        return convertToObject(parameter, type, true);
    }

    private static ConversionResult convertToObject(Object parameter, Class<?> type, boolean checkType) {
        LOGGER.debug("Trying to convert argument {} into something compatible with {}", parameter, type);
        // check if the required class is supported
        if(checkType && !FrpcTypes.isSupportedRawType(type)) {
            return error(type);
        }
        // if the argument is null
        if(parameter == null) {
            // it will be compatible with any type except for primitives
            return type.isPrimitive() ? error(
                    "Cannot convert null value to primitive type " + type.getSimpleName()) : success(
                    null);
        }
        // get boxed desired type
        Class<?> boxedType = ClassUtils.primitiveToWrapper(type);
        // check if argument is instance of given type or if they are compatible numbers
        if(boxedType.isInstance(parameter)) {
            LOGGER.debug("Argument {} is type-compatible with required method parameter type {}, no conversion needed.",
                    parameter, type);
            return success(parameter);
        }
        // get type of the parameter
        Class<?> boxedParameterType = ClassUtils.primitiveToWrapper(parameter.getClass());
        // check for compatible integer and floating point types
        if((boxedType == Long.class && boxedParameterType == Integer.class)
                || (boxedType == Double.class && boxedParameterType == Float.class)) {
            LOGGER.debug("Argument {} is numeric type with lower precision than method parameter type {}, relying on"
                    + " implicit widening conversion.", parameter, type);
            return success(parameter);
        }
        // if the parameter is an array
        if(boxedParameterType.isArray()) {
            // if the desired type is array as well
            if(type.isArray()) {
                // convert it to array
                return convertToArray(parameter, type.getComponentType());
            }
            if(FrpcTypes.isSupportedCollectionType(type)) {
                // convert it to collection
                return convertToCollection(parameter, type, Object.class);
            }
            // cannot convert array to anything else then array or a collection
            return error(
                    "Cannot convert array type to anything else than an array or a collection. Given type: " + type
                            .getCanonicalName() + ", desired type: " + boxedParameterType.getCanonicalName());
        }
        // if the parameter is a map
        if(boxedParameterType == Map.class) {
            // the only supported type for conversion is any of supported map types
            if(FrpcTypes.isSupportedMapType(type)) {
                // convert it to Map<Object, Object>
                return convertToMap(parameter, type, Object.class, Object.class);
            }
            // cannot convert map to anything but another map
            return error(
                    "Cannot convert struct type to anything else than a map. Given type: " + type
                            .getCanonicalName() + ", desired type: " + boxedParameterType.getCanonicalName());
        }

        /* date time types */
        if(boxedParameterType == Calendar.class) {
            // calendar to Date
            if(type == Date.class) {
                return success(FrpcTypes.calendarToDate(((Calendar) parameter)));
            }
            // calendar to LocalDateTime
            if(type == LocalDateTime.class) {
                return success(FrpcTypes.calendarToLocalDateTime(((Calendar) parameter)));
            }
            // calendar to ZonedDateTime
            if(type == ZonedDateTime.class) {
                return success(FrpcTypes.calendarToZonedDateTime(((Calendar) parameter)));
            }
            // cannot convert calendar to anything else than Date, LocalDateTime or ZonedDateTime
            return error("Cannot convert " + Calendar.class.getCanonicalName() + " to " + boxedParameterType
                    .getCanonicalName() + ", the only supported conversions for Calendar are to Date, LocalDateTime and ZonedDateTime");
        }
        // if none of the above is true, given argument is not compatible with given type
        return error("No applicable conversion from " + parameter.getClass().getCanonicalName() + " to " + type.getCanonicalName());
    }

    private static ConversionResult error(Class<?> unsupported) {
        return error("Type " + unsupported.getCanonicalName() + " is not supported by this framework");
    }

    private static ConversionResult error(String message) {
        return new ConversionResult(false, message, null);
    }

    private static ConversionResult error(String message, ConversionResult cause) {
        return new ConversionResult(false, message + " Cause: \n" + cause.errorMessage, null);
    }

    private static ConversionResult success(Object converted) {
        return new ConversionResult(true, null, converted);
    }

}
