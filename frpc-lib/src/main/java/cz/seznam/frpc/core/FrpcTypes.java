package cz.seznam.frpc.core;

import org.apache.commons.lang3.ClassUtils;

import java.util.*;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcTypes {

    public static final Set<Class<?>> COMPATIBLE_TYPES;

    static {
        Set<Class<?>> compatibleClasses = new HashSet<>(
                Arrays.asList(Integer.class, Long.class, Float.class, Double.class, String.class, Map.class, List.class,
                        Set.class, Calendar.class, Object.class));
        COMPATIBLE_TYPES = Collections.unmodifiableSet(compatibleClasses);
    }

    public static boolean isCompatibleType(Class<?> examined) {
        return COMPATIBLE_TYPES.contains(ClassUtils.primitiveToWrapper(examined)) || examined.isArray();
    }

}
