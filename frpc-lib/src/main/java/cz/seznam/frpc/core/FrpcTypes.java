package cz.seznam.frpc.core;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcTypes {

    private static final Set<Class<?>> SUPPORTED_PRIMITIVE_TYPES;
    private static final Set<Class<?>> OTHER_COMPATIBLE_TYPES;
    private static final Map<Class<?>, Supplier<?>> COLLECTION_INTERFACE_INSTANTIATORS;
    private static final Set<Class<?>> OTHER_SUPPORTED_COLLECTION_TYPES;
    private static final Map<Class<?>, Supplier<?>> MAP_INTERFACE_INSTANTIATORS;
    private static final Set<Class<?>> OTHER_SUPPORTED_MAP_TYPES;


    static {
        Set<Class<?>> compatibleClasses = new HashSet<>(
                Arrays.asList(String.class, Boolean.class, Calendar.class, Date.class, LocalDateTime.class,
                        ZonedDateTime.class, Object.class));
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
        if(Map.class.isAssignableFrom(examined) && !examined.isInterface() && !Modifier.isAbstract(examined.getModifiers())) {
            try {
                // check if it has a default no-arg constructor
                Constructor defaultConstructor = examined.getConstructor();
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
        if(Collection.class.isAssignableFrom(examined) && !examined.isInterface() && !Modifier.isAbstract(examined.getModifiers())) {
            try {
                // check if it has a default no-arg constructor
                Constructor defaultConstructor = examined.getConstructor();
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

    public static Map instantiateMap(Class<?> mapType) {
        Supplier<?> supplier = MAP_INTERFACE_INSTANTIATORS.get(mapType);
        if(supplier != null) {
            return (Map) supplier.get();
        }
        // try other map implementations
        if(OTHER_SUPPORTED_MAP_TYPES.contains(mapType)) {
            try {
                return (Map) mapType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
            }
        }
        throw new IllegalArgumentException("Cannot instantiate " + mapType.getCanonicalName());
    }

    public static Collection instantiateCollection(Class<?> collectionType) {
        Supplier<?> supplier = COLLECTION_INTERFACE_INSTANTIATORS.get(collectionType);
        if(supplier != null) {
            return (Collection) supplier.get();
        }
        // try other collections
        if(OTHER_SUPPORTED_COLLECTION_TYPES.contains(collectionType)) {
            try {
                return (Collection) collectionType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
            }
        }
        throw new IllegalArgumentException("Cannot instantiate " + collectionType.getCanonicalName());
    }

    public static Date calendarToDate(Calendar calendar) {
        return Objects.requireNonNull(calendar).getTime();
    }

    public static LocalDateTime calendarToLocalDateTime(Calendar calendar) {
        Objects.requireNonNull(calendar);
        // convert Calendar -> Instant -> LocalDateTime
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    public static ZonedDateTime calendarToZonedDateTime(Calendar calendar) {
        Objects.requireNonNull(calendar);
        // convert Calendar -> Instant -> LocalDateTime
        return ZonedDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

}
