package cz.seznam.frpc.core;

import cz.seznam.frpc.core.transport.FrpcFault;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcTypesConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrpcTypesConverter.class);

    private static final Set<Class<?>> SUPPORTED_PRIMITIVE_TYPES;
    private static final Set<Class<?>> OTHER_COMPATIBLE_TYPES;
    private static final Map<Class<?>, Supplier<?>> COLLECTION_INTERFACE_INSTANTIATORS;
    private static final Set<Class<?>> OTHER_SUPPORTED_COLLECTION_TYPES;
    private static final Map<Class<?>, Supplier<?>> MAP_INTERFACE_INSTANTIATORS;
    private static final Set<Class<?>> OTHER_SUPPORTED_MAP_TYPES;

    static {
        Set<Class<?>> compatibleClasses = new HashSet<>(
                Arrays.asList(String.class, Boolean.class, Calendar.class, Date.class, LocalDateTime.class,
                        ZonedDateTime.class, Object.class, FrpcFault.class));
        OTHER_COMPATIBLE_TYPES = Collections.unmodifiableSet(compatibleClasses);

        Set<Class<?>> primitiveTypes = new HashSet<>(
                Arrays.asList(boolean.class, int.class, long.class, float.class, double.class));
        SUPPORTED_PRIMITIVE_TYPES = Collections.unmodifiableSet(primitiveTypes);

        Map<Class<?>, Supplier<?>> collectionInterfaceInstantiators = new HashMap<>();
        collectionInterfaceInstantiators.put(List.class, ArrayList::new);
        collectionInterfaceInstantiators.put(Set.class, HashSet::new);
        collectionInterfaceInstantiators.put(SortedSet.class, TreeSet::new);
        collectionInterfaceInstantiators.put(NavigableSet.class, TreeSet::new);
        collectionInterfaceInstantiators.put(Queue.class, LinkedList::new);
        collectionInterfaceInstantiators.put(Deque.class, LinkedList::new);
        collectionInterfaceInstantiators.put(Map.class, HashMap::new);
        collectionInterfaceInstantiators.put(SortedMap.class, TreeMap::new);
        collectionInterfaceInstantiators.put(NavigableMap.class, TreeMap::new);
        COLLECTION_INTERFACE_INSTANTIATORS = collectionInterfaceInstantiators;

        Map<Class<?>, Supplier<?>> mapInterfaceInstantiators = new HashMap<>();
        mapInterfaceInstantiators.put(Map.class, HashMap::new);
        mapInterfaceInstantiators.put(SortedMap.class, TreeMap::new);
        mapInterfaceInstantiators.put(NavigableMap.class, TreeMap::new);
        MAP_INTERFACE_INSTANTIATORS = mapInterfaceInstantiators;


        OTHER_SUPPORTED_COLLECTION_TYPES = new HashSet<>();
        OTHER_SUPPORTED_MAP_TYPES = new HashSet<>();
    }

    public static boolean isSupportedRawType(Class<?> examined) {
        return isSupportedPrimitiveOrWrapper(examined) || examined.isArray() || OTHER_COMPATIBLE_TYPES
                .contains(examined) || isSupportedCollectionType(examined) || isSupportedMapType(examined);
    }

    public static boolean isSupportedPrimitiveOrWrapper(Class<?> examined) {
        return SUPPORTED_PRIMITIVE_TYPES.contains(examined) || SUPPORTED_PRIMITIVE_TYPES
                .contains(ClassUtils.wrapperToPrimitive(examined));
    }

    public static boolean isSupportedMapType(Class<?> examined) {
        // check for the class being one of the supported map interfaces or other supported map implementation
        if (MAP_INTERFACE_INSTANTIATORS.containsKey(examined) || OTHER_SUPPORTED_MAP_TYPES.contains(examined)) {
            return true;
        }
        // check if it is a collection implementation
        if (Map.class.isAssignableFrom(examined) && !examined.isInterface() && !Modifier
                .isAbstract(examined.getModifiers())) {
            try {
                // check if it has a default no-arg constructor
                examined.getConstructor();
                // it does, add this class to set of compatible collection types
                OTHER_SUPPORTED_MAP_TYPES.add(examined);
                // and return true
                return true;
            } catch (NoSuchMethodException e) {
            }
        }
        // nope, this is not a supported map type
        return false;
    }

    public static boolean isSupportedCollectionType(Class<?> examined) {
        // check for the class being one of the supported collection interfaces or other supported collection
        // implementation
        if (COLLECTION_INTERFACE_INSTANTIATORS.containsKey(examined) || OTHER_SUPPORTED_COLLECTION_TYPES
                .contains(examined)) {
            return true;
        }
        // check if it is a collection implementation
        if (Collection.class.isAssignableFrom(examined) && !examined.isInterface() && !Modifier
                .isAbstract(examined.getModifiers())) {
            try {
                // check if it has a default no-arg constructor
                examined.getConstructor();
                // it does, add this class to set of compatible collection types
                OTHER_SUPPORTED_COLLECTION_TYPES.add(examined);
                // and return true
                return true;
            } catch (NoSuchMethodException e) {
            }
        }
        // nope, this is not a supported collection type
        return false;
    }

    public static Date calendarToDate(Calendar calendar) {
        return Objects.requireNonNull(calendar).getTime();
    }

    public static LocalDateTime calendarToLocalDateTime(Calendar calendar) {
        Objects.requireNonNull(calendar);
        // convert Calendar -> Instant -> LocalDateTime
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }

    public static ZonedDateTime calendarToZonedDateTime(Calendar calendar) {
        Objects.requireNonNull(calendar);
        // convert Calendar -> Instant -> LocalDateTime
        return ZonedDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    public static Object[] checkAndConvertMethodParameters(String requestMethodName, Type[] methodParameterTypes,
                                                           Object[] parameters) {
        LOGGER.debug("Trying to convert arguments {} given to method \"{}\" to these parameter types: {}",
                parameters, requestMethodName, methodParameterTypes);

        // check if there is at least as many arguments as their types
        if (methodParameterTypes.length < parameters.length) {
            LOGGER.warn("Too many arguments given to method \"{}\", {} arguments required but {} given. Ignoring " +
                            "superfluous {} parameters.", requestMethodName, methodParameterTypes.length, parameters.length,
                    methodParameterTypes.length - parameters.length);
        } else if (methodParameterTypes.length > parameters.length) {
            LOGGER.error("Too few arguments given to method \"{}\", {} arguments required but only these {} " +
                            "arguments" + " were given: {}", requestMethodName, methodParameterTypes.length,
                    parameters.length, parameters);
            throw new IllegalArgumentException("Too few arguments given to method \"" + requestMethodName + "\", " +
                    methodParameterTypes.length + " required but only " + parameters.length + " given.");
        }

        // try to convert all parameters to given types
        Object[] arguments = new Object[methodParameterTypes.length];
        for (int i = 0; i < arguments.length; i++) {
            // if the argument is null, just store it in the arguments array
            arguments[i] = convertParameter(methodParameterTypes, i, parameters[i]);
        }
        // return converted arguments
        return arguments;
    }

    public static Object convertToCompatibleInstanceOrThrowException(Object object, Type type) {
        // try to convert the argument into something compatible with current parameter type
        ConversionResult conversionResult = convertToCompatibleInstance(object, type);
        // if we cannot convert the parameter into a value compatible with required type, throw an exception
        if (!conversionResult.isSuccess()) {
            throw new IllegalArgumentException(
                    "Error while converting value " + object + ", error message: " + conversionResult
                            .getErrorMessage());
        }
        // return converted value
        return conversionResult.getErrorMessage();
    }

    public static ConversionResult convertToCompatibleInstance(Object object, Type type) {
        // check the method parameter type implementation
        if (type instanceof Class) {
            // it's a simple class, convert it to object right away
            return convertToObject(object, (Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            // it's a parameterized type, check it's raw type and parameter arguments
            ParameterizedType parameterizedType = (ParameterizedType) type;
            // check that the raw type is supported
            Class<?> rawType = (Class) parameterizedType.getRawType();
            if (!FrpcTypesConverter.isSupportedRawType(rawType)) {
                return error(rawType);
            }
            // get actual type arguments
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            // do the conversion
            ConversionResult result;
            // check whether it is a map or a collection
            if (FrpcTypesConverter.isSupportedMapType(rawType)) {
                // try to convert it to type-safe map
                result = convertToMap(object, rawType, typeArguments[0], typeArguments[1]);
            } else if (FrpcTypesConverter.isSupportedCollectionType(rawType)) {
                // try to convert it to collection
                result = convertToCollection(object, rawType, typeArguments[0]);
            } else {
                throw new IllegalStateException("Unsupported parameterized type " + parameterizedType
                        .getTypeName() + " was reported as supported by " + FrpcTypesConverter.class.getSimpleName());
            }
            // if the result is an error, describe it and return an error
            return describeErrorOrReturnSuccess(result, object, parameterizedType);
        } else if (type instanceof GenericArrayType) {
            // generic array type
            GenericArrayType genericArrayType = ((GenericArrayType) type);
            // try to convert it to array of given component type
            ConversionResult conversionResult = convertToArray(object, genericArrayType.getGenericComponentType());
            // if the result is an error, describe it and return an error
            return describeErrorOrReturnSuccess(conversionResult, object, genericArrayType);
        } else if (type instanceof TypeVariable) {
            return error("Type variables are not supported");
        } else if (type instanceof WildcardType) {
            return error("Wildcard types are not supported");
        }
        // generic error
        return error("Unknown Type implementation " + type.getClass().getCanonicalName());
    }

    private static Object convertParameter(Type[] methodParameterTypes, int i, Object parameter) {
        // try to convert the argument into something compatible with current parameter type
        ConversionResult conversionResult = convertToCompatibleInstance(parameter, methodParameterTypes[i]);
        // if we cannot convert the parameter into a value compatible with required type, throw an exception
        if (!conversionResult.isSuccess()) {
            throw new IllegalArgumentException(
                    "Error while reading argument #" + (i + 1) + ", error message: " + conversionResult
                            .getErrorMessage());
        }
        // add it to the array of parameters
        LOGGER.debug("Setting {} as argument #{}", conversionResult.getConverted(), i + 1);
        return conversionResult.getConverted();
    }

    @SuppressWarnings("unchecked")
    private static ConversionResult convertToMap(Object parameter, Class<?> mapType,
                                                 Type keysType, Type valuesType) {
        // if the parameter is null, return it right away
        if (parameter == null) {
            return success(null);
        }
        // check that the argument actually is a map
        if (!(parameter instanceof Map)) {
            return error("Cannot convert from " + parameter.getClass()
                    .getCanonicalName() + " to map, incompatible types");
        }
        // cast the parameter to map
        Map<?, ?> parameterAsMap = ((Map) parameter);
        // create the map
        Map map = FrpcTypesConverter.instantiateMap(mapType);
        // iterate all entries in the original map
        for (Map.Entry<?, ?> e : parameterAsMap.entrySet()) {
            // convert key and value
            ConversionResult keyConversionResult = convertToCompatibleInstance(e.getKey(), keysType);
            if (!keyConversionResult.isSuccess()) {
                return keyConversionResult;
            }
            ConversionResult valueConversionResult = convertToCompatibleInstance(e.getValue(), valuesType);
            if (!valueConversionResult.isSuccess()) {
                return valueConversionResult;
            }
            // put them to the map
            map.put(keyConversionResult.getConverted(), valueConversionResult.getConverted());
        }
        // return the map
        return success(map);
    }

    private static ConversionResult convertToArray(Object parameter, Type componentType) {
        // if the parameter is null, return it right away
        if (parameter == null) {
            return success(null);
        }

        // we are expecting an Object[]
        if (parameter.getClass() != Object[].class) {
            return error("Cannot convert from " + parameter.getClass()
                    .getCanonicalName() + " to array, incompatible types");
        }
        // cast the parameter to Object[]
        Object[] parameterAsArray = ((Object[]) parameter);

        // determine component type of the array to be created
        Class componentClass;

        // check if the component type is either a class or a parameterized type and allocate new array properly
        if (componentType instanceof Class) {
            componentClass = ((Class) componentType);
        } else if (componentType instanceof ParameterizedType) {
            componentClass = ((Class) ((ParameterizedType) componentType).getRawType());
        } else {
            return error("Type " + componentType.getTypeName() + " is not supported component array type");
        }

        // get length of the original array
        int length = Array.getLength(parameter);
        // allocate new array of proper length
        Object newArray = Array.newInstance(componentClass, length);
        // iterate all elements of the original array
        for (int i = 0; i < length; i++) {
            // try to convert each of them
            ConversionResult converted = convertToCompatibleInstance(parameterAsArray[i], componentType);
            // if the conversion failed, return an error
            if (!converted.isSuccess()) {
                return converted;
            }
            // set the converted value into the new array
            Array.set(newArray, i, converted.getConverted());
        }
        // return newly created array
        return success(newArray);
    }

    @SuppressWarnings("unchecked")
    private static ConversionResult convertToCollection(Object parameter, Class<?> collectionType, Type valuesType) {
        // if the parameter is null, return it right away
        if (parameter == null) {
            return success(null);
        }

        // we are expecting an Object[]
        if (parameter.getClass() != Object[].class) {
            return error("Cannot convert from " + parameter.getClass()
                    .getCanonicalName() + " to collection, incompatible types");
        }
        // cast the parameter to Object[]
        Object[] parameterAsArray = ((Object[]) parameter);

        // instantiate the collection
        Collection collection = FrpcTypesConverter.instantiateCollection(collectionType);

        // get length of the original array
        int length = Array.getLength(parameter);
        // iterate all elements of the original array
        for (int i = 0; i < length; i++) {
            // try to convert each of them
            ConversionResult converted = convertToCompatibleInstance(parameterAsArray[i], valuesType);
            // if the conversion failed, return an error
            if (!converted.isSuccess()) {
                return converted;
            }
            // set the converted value into the new array
            collection.add(converted.getConverted());
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
        if (checkType && !FrpcTypesConverter.isSupportedRawType(type)) {
            return error(type);
        }
        // if the argument is null
        if (parameter == null) {
            // it will be compatible with any type except for primitives
            return type.isPrimitive() ? error(
                    "Cannot convert null value to primitive type " + type.getSimpleName()) : success(
                    null);
        }
        // get boxed desired type
        Class<?> boxedType = ClassUtils.primitiveToWrapper(type);
        // check if argument is instance of given type or if they are compatible numbers
        if (boxedType.isInstance(parameter)) {
            LOGGER.debug("Argument {} is type-compatible with required method parameter type {}, no conversion needed.",
                    parameter, type);
            return success(parameter);
        }
        // get type of the parameter
        Class<?> boxedParameterType = ClassUtils.primitiveToWrapper(parameter.getClass());
        // check for compatible integer and floating point types
        if (boxedType == Long.class && boxedParameterType == Integer.class) {
            return success(((Integer) parameter).longValue());
        }
        if (boxedType == Double.class && boxedParameterType == Float.class) {
            return success(((Float) parameter).doubleValue());
        }
        // if the parameter is an array
        if (boxedParameterType.isArray()) {
            // if the desired type is array as well
            if (type.isArray()) {
                // convert it to array
                return convertToArray(parameter, type.getComponentType());
            }
            if (FrpcTypesConverter.isSupportedCollectionType(type)) {
                // convert it to collection
                return convertToCollection(parameter, type, Object.class);
            }
            // cannot convert array to anything else then array or a collection
            return error(
                    "Cannot convert array type to anything else than an array or a collection. Given type: " + type
                            .getCanonicalName() + ", desired type: " + boxedParameterType.getCanonicalName());
        }
        // if the parameter is a map
        if (boxedParameterType == Map.class) {
            // the only supported type for conversion is any of supported map types
            if (FrpcTypesConverter.isSupportedMapType(type)) {
                // convert it to Map<Object, Object>
                return convertToMap(parameter, type, Object.class, Object.class);
            }
            // cannot convert map to anything but another map
            return error(
                    "Cannot convert struct type to anything else than a map. Given type: " + type
                            .getCanonicalName() + ", desired type: " + boxedParameterType.getCanonicalName());
        }

        /* date time types */
        if (boxedParameterType == Calendar.class) {
            // calendar to Date
            if (type == Date.class) {
                return success(FrpcTypesConverter.calendarToDate(((Calendar) parameter)));
            }
            // calendar to LocalDateTime
            if (type == LocalDateTime.class) {
                return success(FrpcTypesConverter.calendarToLocalDateTime(((Calendar) parameter)));
            }
            // calendar to ZonedDateTime
            if (type == ZonedDateTime.class) {
                return success(FrpcTypesConverter.calendarToZonedDateTime(((Calendar) parameter)));
            }
            // cannot convert calendar to anything else than Date, LocalDateTime or ZonedDateTime
            return error("Cannot convert " + Calendar.class.getCanonicalName() + " to " + boxedParameterType
                    .getCanonicalName() + ", the only supported conversions for Calendar are to Date, LocalDateTime and ZonedDateTime");
        }
        // if none of the above is true, given argument is not compatible with given type
        return error("No applicable conversion from " + parameter.getClass().getCanonicalName() + " to " + type
                .getCanonicalName());
    }

    private static ConversionResult error(Class<?> unsupported) {
        return error("Type " + unsupported.getCanonicalName() + " is not supported by this framework");
    }

    private static ConversionResult error(String message) {
        return new ConversionResult(false, message, null);
    }

    private static ConversionResult error(String message, ConversionResult cause) {
        return new ConversionResult(false, message + " Cause: \n" + cause.getErrorMessage(), null);
    }

    private static ConversionResult success(Object converted) {
        return new ConversionResult(true, null, converted);
    }

    private static ConversionResult describeErrorOrReturnSuccess(ConversionResult conversionResult,
                                                                 Object parameter, Type type) {
        if (!conversionResult.isSuccess()) {
            return error("Error while converting value " + parameter + " to " + type
                    .getTypeName(), conversionResult);
        } else {
            return conversionResult;
        }
    }

    private static Map instantiateMap(Class<?> mapType) {
        Supplier<?> supplier = MAP_INTERFACE_INSTANTIATORS.get(mapType);
        if (supplier != null) {
            return (Map) supplier.get();
        }
        // try other map implementations
        if (OTHER_SUPPORTED_MAP_TYPES.contains(mapType)) {
            try {
                return (Map) mapType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
            }
        }
        throw new IllegalArgumentException("Cannot instantiate " + mapType.getCanonicalName());
    }

    private static Collection instantiateCollection(Class<?> collectionType) {
        Supplier<?> supplier = COLLECTION_INTERFACE_INSTANTIATORS.get(collectionType);
        if (supplier != null) {
            return (Collection) supplier.get();
        }
        // try other collections
        if (OTHER_SUPPORTED_COLLECTION_TYPES.contains(collectionType)) {
            try {
                return (Collection) collectionType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
            }
        }
        throw new IllegalArgumentException("Cannot instantiate " + collectionType.getCanonicalName());
    }

}
