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

    private Map<String, FrpcMethodHandler> mapping = new HashMap<>();

    public void addHandler(String key, Object handler) {
        Objects.requireNonNull(handler);
        createMapping(key, Objects.requireNonNull(handler).getClass(), () -> handler);
    }

    public void addHandler(String key, MethodMetaDataProvider methodMetaDataProvider, FrpcHandler handler) {
        mapping.put(Objects.requireNonNull(key),
                new FrpcMethodHandler(Objects.requireNonNull(methodMetaDataProvider), Objects.requireNonNull(handler)));
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
        // create MethodMetaDataProvider
        MethodMetaDataProvider nameToTypesMapper = new ReflectiveMethodMetaDataProvider(methodLocator);
        // create the handler
        ReflectiveFrpcHandler handler = new ReflectiveFrpcHandler(supplier, methodLocator);
        // save it into the map
        mapping.put(Objects.requireNonNull(key), new FrpcMethodHandler(nameToTypesMapper, handler));
    }

    Map<String, FrpcMethodHandler> getMapping() {
        return mapping;
    }

}
