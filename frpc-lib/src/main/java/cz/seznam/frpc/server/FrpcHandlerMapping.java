package cz.seznam.frpc.server;

import cz.seznam.frpc.FrpcIgnore;
import cz.seznam.frpc.FrpcName;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcHandlerMapping {

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private Map<String, FrpcHandler> mapping = new HashMap<>();

    public void addHandler(String key, Object handler) {
        Objects.requireNonNull(handler);
        createMapping(key, Objects.requireNonNull(handler).getClass(), () -> handler);
    }

    public void addHandler(String key, FrpcHandler handler) {
        mapping.put(Objects.requireNonNull(key), Objects.requireNonNull(handler));
    }

    public <T> void addHandler(String key, Class<T> handlerClass, Supplier<T> handlerSupplier) {
        createMapping(key, Objects.requireNonNull(handlerClass), Objects.requireNonNull(handlerSupplier));
    }

    public void addHandler(String key, Class<?> handlerClass) {
        // check that given class has default no-arg constructor
        try {
            Objects.requireNonNull(handlerClass).getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Given class " + handlerClass.getSimpleName() + " has no default no-arg constructor");
        }

        // create mapping
        createMapping(key, handlerClass, () -> {
            try {
                return handlerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Cannot instantiate class " + handlerClass.getSimpleName(), e);
            }
        });
    }

    public boolean removeHandler(String key) {
        return mapping.remove(key) != null;
    }

    private void createMapping(String key, Class<?> handlerClass, Supplier<?> supplier) {
        // map methods of given class
        Map<String, Method> methods = mapMethods(handlerClass);
        // create the wrapper
        ReflectiveFrpcHandlerWrapper wrapper = new ReflectiveFrpcHandlerWrapper(supplier, methods);
        // save it into the map
        mapping.put(Objects.requireNonNull(key), wrapper);
    }

    private Map<String, Method> mapMethods(Class<?> handlerClass) {
        // get declared methods of given class
        Map<String, Method> methodsByName = Arrays.stream(handlerClass.getDeclaredMethods())
                // filter out those which are static, non-public or annotated by @FrpcIgnore
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())
                        && !m.isAnnotationPresent(FrpcIgnore.class))
                // and map them by their names
                .collect(Collectors.toMap(m -> {
                    FrpcName frpcName = m.getAnnotation(FrpcName.class);
                    return frpcName == null ? m.getName() : frpcName.value();
                }, Function.identity(), (x, y) -> {
                    // if method name occurs more than once, it's an error
                    throw new IllegalArgumentException("Class " + handlerClass.getSimpleName()
                            + " contains ambiguous methods named " + x.getName());
                }));

        // then create wrapper for this supplier
        return methodsByName;
    }

}
