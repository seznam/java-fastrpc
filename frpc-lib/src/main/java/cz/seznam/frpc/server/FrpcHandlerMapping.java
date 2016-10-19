package cz.seznam.frpc.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;


/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcHandlerMapping {

    // TODO: add dependency to SLF4J or something like that
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private Map<String, Pair<MethodNameToParameterTypesMapper, FrpcHandler>> mapping = new HashMap<>();

    public void addHandler(String key, Object handler) {
        Objects.requireNonNull(handler);
        createMapping(key, Objects.requireNonNull(handler).getClass(), () -> handler);
    }

    public void addHandler(String key, MethodNameToParameterTypesMapper methodNameToParameterTypesMapper, FrpcHandler handler) {
        mapping.put(Objects.requireNonNull(key),
                new Pair<>(Objects.requireNonNull(methodNameToParameterTypesMapper), Objects.requireNonNull(handler)));
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
        // create MethodLocator for given class
        MethodLocator methodLocator = new ReflectiveMethodLocator(handlerClass);
        // create MethodNameToParameterTypesMapper
        MethodNameToParameterTypesMapper nameToTypesMapper = new ReflectiveMethodNameToParameterTypesMapper(methodLocator);
        // create the handler
        ReflectiveFrpcHandler handler = new ReflectiveFrpcHandler(supplier, methodLocator);
        // save it into the map
        mapping.put(Objects.requireNonNull(key), new Pair<>(nameToTypesMapper, handler));
    }

    Map<String, Pair<MethodNameToParameterTypesMapper, FrpcHandler>> getMapping() {
        return mapping;
    }

}
