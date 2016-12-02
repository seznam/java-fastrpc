package cz.seznam.frpc.core;

import cz.seznam.frpc.core.deserialization.FrpcUnmarshaller;
import cz.seznam.frpc.core.serialization.FrpcMarshaller;
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
 * Core component of the framework which handles conversion of POJOs from/to objects writable/readable by
 * {@link FrpcMarshaller} and {@link FrpcUnmarshaller} respectively.
 *
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
                Arrays.asList(String.class, Calendar.class, Date.class, LocalDateTime.class,
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
        COLLECTION_INTERFACE_INSTANTIATORS = collectionInterfaceInstantiators;

        Map<Class<?>, Supplier<?>> mapInterfaceInstantiators = new HashMap<>();
        mapInterfaceInstantiators.put(Map.class, HashMap::new);
        mapInterfaceInstantiators.put(SortedMap.class, TreeMap::new);
        mapInterfaceInstantiators.put(NavigableMap.class, TreeMap::new);
        MAP_INTERFACE_INSTANTIATORS = mapInterfaceInstantiators;


        OTHER_SUPPORTED_COLLECTION_TYPES = new HashSet<>();
        OTHER_SUPPORTED_MAP_TYPES = new HashSet<>();
    }

    /**
     * Checks whether given class represents a <i>raw</i> type (that is non-generic type) supported by this converter.
     * <p/>
     * If a class is <i>supported</i>, then this converter is capable of converting its instances into objects which are
     * compatible with {@code FastRPC} protocol.
     * <p/>
     * Supported raw types are:
     * <ul>
     * <li>Java primitive types</li>
     * <li>Wrappers of Java primitive types ({@code Integer}, {@code Long}, etc.)</li>
     * <li>Array types representable by {@code Class}</li>
     * <li>Many collection types (see {@link #isSupportedCollectionType(Class)}</li>
     * <li>Many {@code Map} types (see {@link #isSupportedMapType(Class)}</li>
     * <li>
     * {@code String}, {@code Calendar}, {@code Date}, {@code LocalDateTime}, {@code ZonedDateTime} and
     * {@code Object}
     * </li>
     * </ul>
     *
     * @param examined class to check for being supported by this converter
     * @return {@code true} if given class is <i>supported</i> by this converter and {@code false} otherwise
     */
    public static boolean isSupportedRawType(Class<?> examined) {
        return isSupportedPrimitiveOrWrapper(examined) || examined.isArray() || OTHER_COMPATIBLE_TYPES
                .contains(examined) || isSupportedCollectionType(examined) || isSupportedMapType(examined);
    }

    /**
     * Checks whether given class represents a primitive type or its wrapper type and is supported by this converter.
     * <p/>
     * If a class is <i>supported</i>, then this converter is capable of converting its instances into objects which are
     * compatible with {@code FastRPC} protocol.
     *
     * @param examined class to check for being supported by this converter
     * @return {@code true} if given class is <i>supported</i> by this converter and {@code false} otherwise
     */
    public static boolean isSupportedPrimitiveOrWrapper(Class<?> examined) {
        return SUPPORTED_PRIMITIVE_TYPES.contains(examined) || SUPPORTED_PRIMITIVE_TYPES
                .contains(ClassUtils.wrapperToPrimitive(examined));
    }

    /**
     * Checks whether given class represents a {@code Map} type or supported by this converter.
     * <p/>
     * If a class is <i>supported</i>, then this converter is capable of converting its instances into objects which are
     * compatible with {@code FastRPC} protocol.
     * <p/>
     * Supported {@code Map} types are:
     * <ul>
     * <li>
     * Common {@code Map} interface from {@code java.util}, specifically:
     * <ul>
     * <li>
     * {@link Map} for which {@link HashMap} gets instantiated
     * </li>
     * <li>
     * {@link SortedMap} and {@link NavigableMap} for which {@link TreeMap} gets instantiated
     * </li>
     * </ul>
     * </li>
     * <li>
     * Any other non-abstract {@link Map} implementations which have accessible default no-arg constructor.
     * </li>
     * </ul>
     *
     * @param examined class to check for being supported {@code Map} type
     * @return {@code true} if given class is a {@code Map} type and is <i>supported</i> by this converter; {@code
     * false} otherwise
     */
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

    /**
     * Checks whether given class represents a {@code Collection} type or supported by this converter.
     * <p/>
     * If a class is <i>supported</i>, then this converter is capable of converting its instances into objects which are
     * compatible with {@code FastRPC} protocol.
     * <p/>
     * Supported {@code Collection} types are:
     * <ul>
     * <li>
     * Common {@code Collection} interface from {@code java.util}, specifically:
     * <ul>
     * <li>
     * {@link List} for which {@link ArrayList} gets instantiated
     * </li>
     * <li>
     * {@link Set} for which {@link HashSet} gets instantiated
     * </li>
     * <li>
     * {@link SortedSet} and {@link NavigableSet} for which {@link TreeSet} gets instantiated
     * </li>
     * <li>
     * {@link Queue} and {@link Deque} for which {@link LinkedList} gets instantiated
     * </li>
     * </ul>
     * </li>
     * <li>
     * Any other non-abstract {@link Collection} implementations which have accessible default no-arg constructor.
     * </li>
     * </ul>
     *
     * @param examined class to check for being supported {@code Collection} type
     * @return {@code true} if given class is a {@code Collection} type and is <i>supported</i> by this converter;
     * {@code false} otherwise
     */
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

    /**
     * Converts given {@code Calendar} to {@code Date}.
     *
     * @param calendar calendar instance to be converted to corresponding {@code Date}
     * @return {@code Date} instance corresponding to date represented by given {@code Calendar}
     */
    public static Date calendarToDate(Calendar calendar) {
        return Objects.requireNonNull(calendar).getTime();
    }

    /**
     * Converts given {@code Calendar} to {@code LocalDateTime}. The conversion is done by converting given {@code
     * Calendar} to {@code Instant} from which the {@code LocalDateTime} instance is created using <strong>system
     * default</strong> time zone. Time zone specified by the calendar is ignored.
     *
     * @param calendar calendar instance to be converted to corresponding {@code LocalDateTime} using <strong>system
     *                 default</strong> time zone
     * @return {@code LocalDateTime} instance corresponding to date represented by given {@code Calendar} w.r.t.
     * <strong>system default</strong> time zone
     */
    public static LocalDateTime calendarToLocalDateTime(Calendar calendar) {
        Objects.requireNonNull(calendar);
        // convert Calendar -> Instant -> LocalDateTime
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Converts given {@code Calendar} to {@code ZonedDateTime}. The conversion is done by converting given {@code
     * Calendar} to {@code Instant} from which the {@code ZonedDateTime} instance is created using time zone
     * <strong>from the calendar</strong>.
     *
     * @param calendar calendar instance to be converted to corresponding {@code LocalDateTime} using time zone
     *                 <strong>from the calendar</strong>
     * @return {@code LocalDateTime} instance corresponding to date represented by given {@code Calendar} w.r.t. time
     * zone <strong>from the calendar</strong>.
     */
    public static ZonedDateTime calendarToZonedDateTime(Calendar calendar) {
        Objects.requireNonNull(calendar);
        // convert Calendar -> Instant -> LocalDateTime
        return ZonedDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    /**
     * Convenience method for converting array of method parameters to required types.
     * <p/>
     * This method assumes that {@code methodParameterTypes} accurately reflect number, ordering and number of
     * parameters of method named {@code fullMethodName}. Assuming that, it checks that the number of {@code parameters}
     * matches exactly the number of required parameters and tries to convert each of them to corresponding required
     * type.
     *
     * @param fullMethodName       <strong>full</strong> {@code FRPC} method name (that is name including the handler
     *                             part)
     * @param methodParameterTypes types of parameters required by the {@code FRPC} method implementation
     * @param parameters           parameters provided by caller of the {@code FRPC} method
     * @return array of given parameters, each converted into required type
     * @throws IllegalArgumentException if there are too few or too many parameters given or if any of given parameters
     *                                  could not be converted to required type
     */
    public static Object[] checkAndConvertMethodParameters(String fullMethodName, Type[] methodParameterTypes,
                                                           Object[] parameters) {
        LOGGER.debug("Trying to convert arguments {} given to method \"{}\" to these parameter types: {}",
                parameters, fullMethodName, methodParameterTypes);

        // check if there is at least as many arguments as their types
        if (methodParameterTypes.length < parameters.length) {
            LOGGER.warn("Too many arguments given to method \"{}\", {} arguments required but {} given. Ignoring " +
                            "superfluous {} parameters.", fullMethodName, methodParameterTypes.length, parameters.length,
                    methodParameterTypes.length - parameters.length);
        } else if (methodParameterTypes.length > parameters.length) {
            LOGGER.error("Too few arguments given to method \"{}\", {} arguments required but only these {} " +
                            "arguments" + " were given: {}", fullMethodName, methodParameterTypes.length,
                    parameters.length, parameters);
            throw new IllegalArgumentException("Too few arguments given to method \"" + fullMethodName + "\", " +
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

    /**
     * Convenience method which calls {@link #convertToCompatibleInstance(Object, Type)} and throws an exception if the
     * {@link ConversionResult} indicates failure.
     *
     * @param object object to be converted to given type
     * @param type   type to convert given object to
     * @return instance of desired type created via conversion from given object
     */
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

    /**
     * Tries to convert given object into (possibly another) object of required type.
     * <p/>
     * <i>Conversion</i> from object of one type to object of another type performed by this method may be done in
     * several ways. For further reference, lets call the object <i>which is to be converted</i> the
     * <strong>source object</strong> and lets call its type <strong>source type</strong>, furthermore lets call the 
     * desired type of resulting object {@code result type} and lets call the object being <i>the result of the 
     * conversion</i> a <strong>result object</strong>.
     * <p/>
     * The conversion then works as follows:
     * <ul>
     *     <li>
     *         <strong>Casting</strong> <br />
     *         If the <strong>result type</strong> is simply a {@link Class} and the <strong>source object</strong> is
     *         {@code instanceof} that class, it is simply casted to that class. The <strong>result object</strong> is
     *         then the very same object as the <strong>source object</strong>. <br/>
     *         This also covers the case when the <strong>source object</strong> is null and the
     *         <strong>result type</strong> is not a primitive type.
     *     </li>
     *     <li>
     *         <strong>Integer -> Long</strong> <br />
     *         If the <strong>source type</strong> is {@link Integer} and the <strong>result type</strong> is
     *         {@link Long}, the <strong>source object</strong> is converted to {@code Long} using
     *         {@link Integer#longValue()}. This works for {@code int} and {@code long} as well.
     *     </li>
     *     <li>
     *         <strong>Float -> Double</strong> <br />
     *         If the <strong>source type</strong> is {@link Float} and the <strong>result type</strong> is
     *         {@link Double}, the <strong>source object</strong> is converted to {@code Long} using
     *         {@link Float#doubleValue()}. This works for {@code float} and {@code double} as well.
     *     </li>
     *     <li>
     *         <strong>Calendar -> other Date types</strong> <br />
     *         If the <strong>source type</strong> is {@link Calendar} and the <strong>result type</strong> is either
     *         {@link Date}, {@link LocalDateTime} or {@link ZonedDateTime} the <strong>source object</strong> is
     *         converted to the <strong>result object</strong> using respective {@code calendarTo...()} method of this
     *         converter.
     *     </li>
     *     <li>
     *         <strong>Maps</strong> <br />
     *         If the <strong>source type</strong> represents a {@code Map} and the <strong>result type</strong> is
     *         {@link #isSupportedMapType(Class) supported map type}, then a new map of <strong>result type</strong> is
     *         instantiated and populated by converted entries of the <strong>source object</strong>. <br />
     *         Keys and values of the <strong>source object</strong> are subjected to this conversion process as well
     *         so that the conversion is fully type-safe. <br />
     *         In case the <strong>result type</strong> is a {@link Class}, then generic information is not available
     *         and it is handled as if parameterized type {@code Map<Object, Object>} was given. <br />
     *         If the map type is provided as a {@link ParameterizedType}, then keys and values of the <strong>source
     *         object</strong> are mapped to their respective types determined from type arguments of provided
     *         parameterized  type.
     *     </li>
     *     <li>
     *         <strong>Collections</strong> <br />
     *         If the <strong>source type</strong> represents a {@code Collection} and the <strong>result type</strong>
     *         is {@link #isSupportedCollectionType(Class) supported collection type}, then a new collection of
     *         <strong>result type</strong> is instantiated and populated by converted elements of the <strong>source
     *         object</strong>. <br />
     *         Elements of the <strong>source object</strong> are subjected to this conversion process as well
     *         so that the conversion is fully type-safe. <br />
     *         In case the <strong>result type</strong> is a {@link Class}, then generic information is not available
     *         and it is handled as if it was parameterized with {@code <Object>}. <br />
     *         If the collection type is provided as a {@link ParameterizedType}, then elements of the <strong>source
     *         object</strong> are mapped to the type determined from type arguments of provided parameterized type.
     *     </li>
     *     <li>
     *         <strong>Arrays -> arrays or collections</strong> <br />
     *         If the <strong>source type</strong> represents an array and the <strong>result type</strong>
     *         is either an array or it is {@link #isSupportedCollectionType(Class) supported collection type}, then a
     *         new array or collection of <strong>result type</strong> is instantiated and populated by converted
     *         elements of the <strong>source object</strong>. <br />
     *         Elements of the <strong>source object</strong> are subjected to this conversion process as well
     *         so that the conversion is fully type-safe. <br />
     *         In case the <strong>result type</strong> is a {@link Class} representing a collection, then generic
     *         information is not available and it is handled as if it was parameterized with {@code <Object>}.
     *         If the collection type is provided as a {@link ParameterizedType}, then elements of the <strong>source
     *         object</strong> are mapped to the type determined from type arguments of provided type. <br />
     *         In case the <strong>result type</strong> is an array type, the <strong>resource object</strong> is
     *         created as an array with the same {@link Class#getComponentType() component type}. This works even for
     *         generic arrays in which case the component type of the newly created array is the <i>raw type</i> of
     *         component type of given generic array type.
     *     </li>
     *     <li>
     *         <strong>Other cases</strong> <br />
     *         If the combination of <strong>source type</strong> and <strong>result type</strong> matches none of the
     *         cases above, then this converter cannot convert <strong>source object</strong> to desired
     *         <strong>result type</strong> and the conversion fails.
     *     </li>
     * </ul>
     *
     * @param object object to be converted to given type
     * @param type type to convert given object into
     * @return instance of {@code ConversionResult} indicating either success or failure of the conversion process
     */
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
