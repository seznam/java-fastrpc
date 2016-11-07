package cz.seznam.frpc.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;


/**
 * Handler mapping ties business logic (so called "handlers") to names of {@code FRPC} methods.
 * A {@code FRPC} method name typically looks like this:
 * <pre>
 *     some.handler.name.method
 * </pre>
 * where the part up to the last dot represents a handler name and the rest represents the name of a method within that
 * handler. That is in the example above, handler name would be {@code some.handler.name} and a method name within that
 * handler would be {@code method}.
 * <p>
 * This class allows to map any class or {@link FrpcMethodMetaDataProvider} - {@link FrpcHandler} pair to its name.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 *
 * @see FrpcMethodMetaDataProvider
 * @see FrpcHandler
 */
public class FrpcHandlerMapping {

    /**
     * Holds the actual mapping from String names to DTOs representing the handlers.
     */
    private Map<String, FrpcMethodMetaDataProviderAndHandler> mapping = new HashMap<>();

    /**
     * Maps given object as a handler to given name. All {@code FRPC} calls with matching handler prefix will result in
     * method invocations on this very instance without any means of external synchronization. It is therefore strongly
     * advised to use thread-safe objects.<br />
     * {@code FRPC} methods provided by this handler will be resolved automatically via reflection using
     * {@link ReflectiveFrpcHandlerMethodLocator}.
     *
     * @param name name to map the handler to
     * @param handler an object to make handler from
     */
    public void addHandler(String name, Object handler) {
        Objects.requireNonNull(handler);
        createMapping(name, Objects.requireNonNull(handler).getClass(), () -> handler);
    }

    public void addHandler(String name, FrpcMethodMetaDataProvider methodMetaDataProvider, FrpcHandler handler) {
        mapping.put(Objects.requireNonNull(name),
                new FrpcMethodMetaDataProviderAndHandler(Objects.requireNonNull(methodMetaDataProvider), Objects.requireNonNull(handler)));
    }

    /**
     * Creates a handler from given class and maps it to given name. Given supplier is used every time a new
     * {@code FRPC} call with matching handler prefix is made and is free to return new instance every time. With this
     * method even stateful, non-thread-safe objects can be used as {@code FRPC} handlers. <br />
     * Be aware though that no external synchronization is provided for instances returned by the supplier, so should it
     * return the same instance every time, it is strongly advised for it to be thread-safe. <br />
     * {@code FRPC} methods provided by this handler will be resolved automatically via reflection using
     * {@link ReflectiveFrpcHandlerMethodLocator}. <br />
     *
     * @param name name to map the handler to
     * @param handlerClass a class to create handler from
     * @param handlerSupplier a supplier used to provide instance(s) of {@code handlerClass}
     *
     * @see #addHandler(String, Class, Supplier)
     */
    public <T> void addHandler(String name, Class<T> handlerClass, Supplier<T> handlerSupplier) {
        createMapping(name, Objects.requireNonNull(handlerClass), Objects.requireNonNull(handlerSupplier));
    }

    /**
     * Creates a handler from given class and maps it to given name. Given class must have accessible no-arg constructor
     * which will be used to create single instance of that class. All {@code FRPC} calls with matching handler prefix
     * will then result in method invocations on that single instance without any means of external synchronization.
     * It is therefore strongly advised to use thread-safe objects.<br />
     * {@code FRPC} methods provided by this handler will be resolved automatically via reflection using
     * {@link ReflectiveFrpcHandlerMethodLocator}. <br />
     *
     * @param name name to map the handler to
     * @param handlerClass a class to create handler from
     *
     * @see #addHandler(String, Class, Supplier)
     */
    public void addHandler(String name, Class<?> handlerClass) {
        // create mapping
        createMapping(name, handlerClass, () -> {
            try {
                return Objects.requireNonNull(handlerClass).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Cannot instantiate class " + handlerClass.getSimpleName(), e);
            }
        });
    }

    /**
     * Removes handler of given name from this mapping.
     *
     * @param name name of the handler to remove
     * @return {@code true} if handler by that name was present in this mapping and was removed as a result of this
     *          operation and {@code false} otherwise
     */
    public boolean removeHandler(String name) {
        return mapping.remove(name) != null;
    }

    private void createMapping(String name, Class<?> handlerClass, Supplier<?> supplier) {
        // create FrpcHandlerMethodLocator for given class
        FrpcHandlerMethodLocator methodLocator = new ReflectiveFrpcHandlerMethodLocator(handlerClass);
        // create FrpcMethodMetaDataProvider
        FrpcMethodMetaDataProvider metaDataProvider = new ReflectiveFrpcMethodMetaDataProvider(methodLocator);
        // create the handler
        ReflectiveFrpcHandler handler = new ReflectiveFrpcHandler(supplier, methodLocator);
        // save it into the map
        mapping.put(Objects.requireNonNull(name), new FrpcMethodMetaDataProviderAndHandler(metaDataProvider, handler));
    }

    /**
     * Returns the map. For internal use only.
     *
     * @return the actual mapping
     */
    Map<String, FrpcMethodMetaDataProviderAndHandler> getMapping() {
        return mapping;
    }

}
