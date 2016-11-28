package cz.seznam.frpc.server;

import cz.seznam.frpc.server.annotations.FrpcIgnore;
import cz.seznam.frpc.server.annotations.FrpcMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link FrpcMethodLocator} which uses reflection to find methods of given class which
 * represent {@code FRPC} method implementations. <br />
 * Once instantiated, {@code ReflectiveFrpcMethodLocator} examines given {@code Class} parameter and creates a
 * map mapping methods by their {@code FRPC} names to actual {@link Method}s. The process works as follows:
 * <ul>
 *     <li>
 *         All public non-static methods <strong>declared in given class only</strong> are examined as possible
 *         {@code FRPC} method implementations.
 *     </li>
 *     <li>
 *         If the method is annotated with {@link FrpcIgnore} then it is ignored.
 *     </li>
 *     <li>
 *         If the method is annotated with {@link FrpcMethod}, then the value of "value" property of the
 *         annotation is used as a name for the method. Otherwise the real name of the method is used.
 *     </li>
 * </ul>
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ReflectiveFrpcMethodLocator implements FrpcMethodLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectiveFrpcMethodLocator.class);

    /**
     * Maps {@code FRPC} method names to actual Java methods.
     */
    private Map<String, Method> methodsByName;

    /**
     * Creates new instance which will examine given {@code Class} and store its methods under their {@code FRPC} names.
     *
     * @param examined class to examine
     */
    public ReflectiveFrpcMethodLocator(Class<?> examined) {
        Objects.requireNonNull(examined, "Given class must not be null");
        this.methodsByName = mapMethods(examined);
    }

    /**
     * Tries to find a {@code Method} corresponding to given name.
     *
     * @param frpcMethodName {@code FRPC} method name to locate a method by
     *
     * @return method corresponding to given name
     * @throws NoSuchMethodException if no method could be found by given name
     */
    @Override
    public Method locateMethodByFrpcName(String frpcMethodName) throws NoSuchMethodException {
        Method method = methodsByName.get(frpcMethodName);
        if(method == null)
            throw new NoSuchMethodException();
        return method;
    }

    private Map<String, Method> mapMethods(Class<?> clazz) {
        // getResult declared methods of given class
        Map<String, Method> methodsByName = Arrays.stream(clazz.getDeclaredMethods())
                // filter out those which are static, non-public or annotated by @FrpcIgnore
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())
                        && !m.isAnnotationPresent(FrpcIgnore.class))
                // and map them by their names
                .collect(Collectors.toMap(m -> {
                    FrpcMethod frpcMethod = m.getAnnotation(FrpcMethod.class);
                    if(frpcMethod == null || StringUtils.isBlank(frpcMethod.value())) {
                        return m.getName();
                    }
                    return frpcMethod.value();
                }, Function.identity(), (x, y) -> {
                    // if method value occurs more than once, it's an error
                    throw new IllegalArgumentException("Class " + clazz.getSimpleName()
                            + " contains ambiguous methods named " + x.getName());
                }));

        LOGGER.debug("Mapped {} methods of class {} as FRPC methods: {}", methodsByName.size(), clazz, methodsByName);

        // then create wrapper for this supplier
        return methodsByName;
    }

    /**
     * Returns mapped methods, for internal use only.
     *
     * @return methods mapped to their {@code FRPC} names
     */
    Map<String, Method> getMethodsByName() {
        return methodsByName;
    }

}
