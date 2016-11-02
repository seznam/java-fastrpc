package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcIgnore;
import cz.seznam.frpc.FrpcName;
import cz.seznam.frpc.FrpcResponse;
import cz.seznam.frpc.FrpcTypes;
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
 * @author David Moidl david.moidl@firma.seznam.cz
 */
class ReflectiveMethodLocator implements MethodLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectiveMethodLocator.class);

    private Map<String, Method> methodsByName;

    public ReflectiveMethodLocator(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Given class must not be null");
        this.methodsByName = mapMethods(clazz);
    }

    @Override
    public Method locateMethod(String methodName) throws NoSuchMethodException {
        Method method = methodsByName.get(methodName);
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
                    FrpcName frpcName = m.getAnnotation(FrpcName.class);
                    return frpcName == null ? m.getName() : frpcName.value();
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
        // if it's not a Map, then @FrpcResponse annotation must be present on this method
        if(returnType != Map.class && !method.isAnnotationPresent(FrpcResponse.class)) {
            throw new UnsupportedReturnTypeException("Method \"" + method.getName() + "\" of class "
                    + method.getDeclaringClass().getSimpleName()
                    + " is not annotated by @" + FrpcResponse.class.getSimpleName() + " and has a return type "
                    + method.getReturnType().getSimpleName() + " which is not a Map. Either add the annotation or make it return a Map.");
        }

        return method;
    }

}
