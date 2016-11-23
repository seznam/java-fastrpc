package cz.seznam.frpc.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Handler mapping ties business logic (so called "handlers") to names of {@code FRPC} methods.
 * A {@code FRPC} method name typically looks like this:
 * <pre>
 *     some.handler.value.method
 * </pre>
 * where the part up to the last dot represents a handler name and the rest represents the name of a method within that
 * handler. That is in the example above, handler name would be {@code some.handler.value} and a method name within that
 * handler would be {@code method}.
 * <p>
 * This class allows to map any class or {@link FrpcMethodNamesProvider} - {@link FrpcMethodMetaDataProvider} -
 * {@link FrpcHandler} triple to its name.
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
    private Map<String, FrpcMethodHandlerAndMethods> mapping = new HashMap<>();

    protected FrpcMethodValidator frpcMethodValidator;

    public FrpcHandlerMapping() {
        this(new DefaultFrpcMethodValidator());
    }

    public FrpcHandlerMapping(FrpcMethodValidator frpcMethodValidator) {
        this.frpcMethodValidator = Objects.requireNonNull(frpcMethodValidator);
    }

    /**
     * Maps given object as a handler to given value. All {@code FRPC} calls with matching handler prefix will result in
     * method invocations on this very instance without any means of external synchronization. It is therefore strongly
     * advised to use thread-safe objects.<br />
     * {@code FRPC} methods provided by this handler will be resolved automatically via reflection using
     * {@link ReflectiveFrpcHandlerMethodLocator}.
     *
     * @param name    value to map the handler to
     * @param handler an object to make handler from
     */
    public void addHandler(String name, Object handler) {
        Objects.requireNonNull(handler);
        createMapping(Objects.requireNonNull(name), Objects.requireNonNull(handler).getClass(), () -> handler, null,
                null);
    }

    public void addHandler(String name, FrpcMethodNamesProvider methodNamesProvider,
                           FrpcMethodMetaDataProvider methodMetaDataProvider, FrpcHandler handler) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(methodNamesProvider);
        Objects.requireNonNull(methodMetaDataProvider);
        Objects.requireNonNull(handler);
        // list all method names
        Set<String> methodNames = methodNamesProvider.listMethodNames();
        // map them to their meta data
        Map<String, FrpcMethodMetaData> methods = new HashMap<>(methodNames.size());
        for (String methodName : methodNames) {
            try {
                FrpcMethodMetaData metaData = methodMetaDataProvider.getMethodMetaData(methodName);
                methods.put(methodName, metaData);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Given " + FrpcMethodNamesProvider.class
                        .getSimpleName() + " listed \"" + methodName +
                        "\" as FPRC method of given handler, yet provided " + FrpcMethodMetaDataProvider.class
                        .getSimpleName() + " does not know any method of that name.");
            }
        }
        // validate methods
        validateMethods(methods, name);
        // create the mapping
        mapping.put(name, new FrpcMethodHandlerAndMethods(methods, handler));
    }

    /**
     * Creates a handler from given class and maps it to given value. Given supplier is used every time a new
     * {@code FRPC} call with matching handler prefix is made and is free to return new instance every time. With this
     * method even stateful, non-thread-safe objects can be used as {@code FRPC} handlers. <br />
     * Be aware though that no external synchronization is provided for instances returned by the supplier, so should it
     * return the same instance every time, it is strongly advised for it to be thread-safe. <br />
     * {@code FRPC} methods provided by this handler will be resolved automatically via reflection using
     * {@link ReflectiveFrpcHandlerMethodLocator}. <br />
     *
     * @param name            value to map the handler to
     * @param handlerClass    a class to create handler from
     * @param handlerSupplier a supplier used to provide instance(s) of {@code handlerClass}
     * @see #addHandler(String, Class, Supplier)
     */
    public <T> void addHandler(String name, Class<T> handlerClass, Supplier<T> handlerSupplier) {
        createMapping(name, Objects.requireNonNull(handlerClass), Objects.requireNonNull(handlerSupplier), null, null);
    }

    /**
     * Creates a handler from given class and maps it to given value. Given class must have accessible no-arg constructor
     * which will be used to create single instance of that class. All {@code FRPC} calls with matching handler prefix
     * will then result in method invocations on that single instance without any means of external synchronization.
     * It is therefore strongly advised to use thread-safe objects.<br />
     * {@code FRPC} methods provided by this handler will be resolved automatically via reflection using
     * {@link ReflectiveFrpcHandlerMethodLocator}. <br />
     *
     * @param name         value to map the handler to
     * @param handlerClass a class to create handler from
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
        }, null, null);
    }

    /**
     * Removes handler of given value from this mapping.
     *
     * @param name value of the handler to remove
     * @return {@code true} if handler by that value was present in this mapping and was removed as a result of this
     * operation and {@code false} otherwise
     */
    public boolean removeHandler(String name) {
        return mapping.remove(name) != null;
    }

    @SuppressWarnings("unchecked")
    private void createMapping(String name, Class<?> handlerClass, Supplier<?> supplier,
                               FrpcMethodResolver frpcMethodResolver,
                               FrpcMethodMetaDataProvider frpcMethodMetaDataProvider) {
        // create FrpcHandlerMethodLocator for given class
        ReflectiveFrpcHandlerMethodLocator methodLocator = new ReflectiveFrpcHandlerMethodLocator(handlerClass);
        // create default method resolver if needed
        FrpcMethodResolver methodResolver = frpcMethodResolver == null ?
                new ReflectiveFrpcMethodResolver(methodLocator) : frpcMethodResolver;
        // create default method metadata provider if needed
        FrpcMethodMetaDataProvider metaDataProvider = frpcMethodMetaDataProvider == null ?
                new ReflectiveFrpcMethodMetaDataProvider(methodLocator) : frpcMethodMetaDataProvider;
        // let the resolver resolve all FRPC methods provided by the handler
        Map<String, FrpcMethodMetaData> methods = methodResolver.resolveFrpcMethods(handlerClass);
        // validate them
        validateMethods(methods, name);
        // create the handler
        ReflectiveFrpcHandler handler = new ReflectiveFrpcHandler(supplier, methodLocator);
        // save it into the map
        mapping.put(Objects.requireNonNull(name), new FrpcMethodHandlerAndMethods(methods, handler));
    }

    private void validateMethods(Map<String, FrpcMethodMetaData> methods, String handlerName) {
        // iterate all methods
        for (Map.Entry<String, FrpcMethodMetaData> entry : methods.entrySet()) {
            // and validate them
            FrpcMethodValidationResult validationResult = frpcMethodValidator.validateFrpcMethod(entry.getValue());
            // if the validation failed
            if (!validationResult.isValid()) {
                // it's an error
                throw new IllegalArgumentException(
                        "Error mapping handler to \"" + handlerName + "\". FRPC method named \"" + entry
                                .getKey() + "\" is invalid. The actual problem is: " + validationResult.getError());
            }
        }
    }


    /**
     * Returns the map. For internal use only.
     *
     * @return the actual mapping
     */
    Map<String, FrpcMethodHandlerAndMethods> getMapping() {
        return mapping;
    }

}
