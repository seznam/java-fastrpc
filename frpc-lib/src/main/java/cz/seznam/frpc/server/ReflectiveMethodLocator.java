package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcIgnore;
import cz.seznam.frpc.FrpcName;

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
        // get declared methods of given class
        Map<String, Method> methodsByName = Arrays.stream(clazz.getDeclaredMethods())
                // filter out those which are static, non-public or annotated by @FrpcIgnore
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())
                        && !m.isAnnotationPresent(FrpcIgnore.class))
                // and map them by their names
                .collect(Collectors.toMap(m -> {
                    FrpcName frpcName = m.getAnnotation(FrpcName.class);
                    return frpcName == null ? m.getName() : frpcName.value();
                }, Function.identity(), (x, y) -> {
                    // if method name occurs more than once, it's an error
                    throw new IllegalArgumentException("Class " + clazz.getSimpleName()
                            + " contains ambiguous methods named " + x.getName());
                }));

        // then create wrapper for this supplier
        return methodsByName;
    }


}
