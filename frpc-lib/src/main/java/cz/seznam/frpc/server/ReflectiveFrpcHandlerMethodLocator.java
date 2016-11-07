package cz.seznam.frpc.server;

import cz.seznam.frpc.common.FrpcTypes;
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
 * Implementation of {@link FrpcHandlerMethodLocator} which uses reflection to find methods of given class which
 * represent {@code FRPC} method implementations. <br />
 * Once instantiated, {@code ReflectiveFrpcHandlerMethodLocator} examines given {@code Class} parameter and creates a
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
 *         If the method is annotated with {@link FrpcMethod}, then the value of "name" property of the
 *         annotation is used as a name for the method. Otherwise the real name of the method is used.
 *     </li>
 *     <li>
 *         If the method returns anything but a {@code Map}, then it checks that <strong>it is annotated</strong>
 *         with {@link FrpcMethod} and that the {@code resultKey} of that annotation <strong>is specified</strong>.
 *     </li>
 * </ul>
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveFrpcHandlerMethodLocator implements FrpcHandlerMethodLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectiveFrpcHandlerMethodLocator.class);

    /**
     * Maps {@code FRPC} method names to actual Java methods.
     */
    private Map<String, Method> methodsByName;

    /**
     * Creates new instance which will examine given {@code Class} and store its methods under their {@code FRPC} names.
     *
     * @param c class to examine
     */
    ReflectiveFrpcHandlerMethodLocator(Class<?> c) {
        Objects.requireNonNull(c, "Given class must not be null");
        this.methodsByName = mapMethods(c);
    }

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
                // check params of each method for being compatible with FRPC
                .map(this::checkMethod)
                // and map them by their names
                .collect(Collectors.toMap(m -> {
                    FrpcMethod frpcMethod = m.getAnnotation(FrpcMethod.class);
                    if(frpcMethod == null || StringUtils.isBlank(frpcMethod.name())) {
                        return m.getName();
                    }
                    return frpcMethod.name();
                }, Function.identity(), (x, y) -> {
                    // if method name occurs more than once, it's an error
                    throw new IllegalArgumentException("Class " + clazz.getSimpleName()
                            + " contains ambiguous methods named " + x.getName());
                }));

        LOGGER.debug("Mapped {} methods of class {} as FRPC methods: {}", methodsByName.size(), clazz, methodsByName);

        // then create wrapper for this supplier
        return methodsByName;
    }

    private Method checkMethod(Method method) {
        // iterate method params and check that all of them are compatible with FRPC
        for(Class<?> parameterType : method.getParameterTypes()) {
            if(!FrpcTypes.isCompatibleType(parameterType)) {
                throw new UnsupportedParameterTypeException("Method \"" + method.getName() + "\" of class "
                        + method.getDeclaringClass().getSimpleName() + " declares parameter of type "
                        + parameterType.getSimpleName() + " which is an unsupported FRPC type.");
            }
        }
        // check result type
        Class<?> returnType = method.getReturnType();
        FrpcMethod frpcMethod = method.getAnnotation(FrpcMethod.class);
        // if it's not a Map, then @FrpcMethod annotation must be present on this method and must have the resultKey
        // property set
        if(returnType != Map.class && (frpcMethod == null || StringUtils.isBlank(frpcMethod.resultKey()))) {
            throw new UnsupportedReturnTypeException("Method \"" + method.getName() + "\" of class "
                    + method.getDeclaringClass().getSimpleName()
                    + " is either not annotated by @" + FrpcMethod.class.getSimpleName()
                    + " or property \"resultKey\" of that annotation is not set."
                    + " Since the method has a return type " + method.getReturnType().getSimpleName()
                    + " (which is not a Map), the \"resultKey\" is needed. Either add the annotation and set"
                    + " the property or make the method return a Map.");
        }

        return method;
    }

}
